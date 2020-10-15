package com.timer;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimerTask;

import com.book.model.Book;
import com.book.model.BookService;
import com.promo.model.Promo;
import com.promo.model.PromoService;
import com.promodetail.model.PromoDetail;
import com.promodetail.model.PromoDetailService;

import tools.Arith;

public class PromoTimerTask extends TimerTask {
	private static final SimpleDateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
	private static final String EFF_PROMO_FORMAT = "%02d";
	private Set<Promo> oldValidPromos = new HashSet<Promo>();
	private PromoService promoService;
	private PromoDetailService promoDetailService;
	private BookService bookService;

	public PromoTimerTask(PromoService promoService, PromoDetailService promoDetailService, BookService bookService) {
		this.promoService = promoService;
		this.promoDetailService = promoDetailService;
		this.bookService = bookService;
	}

	@Override
	public void run() {
		// 執行時間資訊
		long start = System.currentTimeMillis();
		Date date = new Date(start);
		String threadName = Thread.currentThread().getName();
		StringBuffer sb = new StringBuffer("\n" + threadName + "促銷事件更新執行時間:\t");
		System.out.print(sb.append(FORMATTER.format(date)).append("\t").append(start));

		// 查詢並建構前一輪有效promo事件(存在B00000000001的欄位中)
		Optional<Book> firstBook = bookService.getByBookID("B00000000001");
		if (firstBook.isPresent()) {
			String temp = firstBook.get().getEffectivePromos();
			if (temp == null) {
				temp = ","; // null無法被.split
			}
			String[] effPromos = temp.split(",");
			for (int i = 0; i < effPromos.length; i++) {
				String promoID = effPromos[i].substring(0, effPromos[i].indexOf(':'));
				Optional<Promo> promo = promoService.getByPromoID(promoID);
				if (promo.isPresent()) {
					oldValidPromos.add(promo.get());
				}
			}
		}

		// 查詢當下有效的promo事件
		Set<Promo> newPromos = promoService.getValidPromos();
		Set<String> validPromoIDs = new HashSet<String>();
		newPromos.forEach(promo -> validPromoIDs.add(promo.getPromoID()));

		// 建構準備移除的promo事件Set
		Set<Promo> removePromos = new HashSet<Promo>();
		removePromos.addAll(oldValidPromos);
		removePromos.removeAll(newPromos);

		// 新的、待被更新的promo事件
		newPromos.removeAll(oldValidPromos);

		// 將目前有效的促銷事件更新至下次的oldValidPromos，並寫回B00000000001的欄位中
		oldValidPromos.removeAll(removePromos);
		oldValidPromos.addAll(newPromos);
		StringBuffer currentValidPromos = new StringBuffer();
		oldValidPromos.forEach(promo -> {
			currentValidPromos.append(promo.getPromoID()).append(":00:00").append(",");
		});

		// =====失效促銷事件處理=====
		removePromos(removePromos);

		// =====生效促銷事件處理=====
		newPromos(newPromos);

		bookService.updateEffPromos(currentValidPromos.toString());
		long spendTime = System.currentTimeMillis() - start;
		System.out.printf("\n完成更新，本輪有效的促銷事件: %s\n花費: %d ms\n", currentValidPromos, spendTime);
	}

	private Double calculateSalePricePromoOrBookBPPromo(double salePrice, int max) {
		double temp = Arith.mul(salePrice, max);
		return Arith.div(temp, 100.0, 0);
	}

	private void removePromos(Set<Promo> removePromos) {
		if (removePromos.size() > 0) {
			System.out.println("\n失效的促銷事件:");
			removePromos.forEach(promo -> {
				System.out.println(promo);
				String promoID = promo.getPromoID();
				List<String> bookIDs = new ArrayList<String>();
				List<PromoDetail> removePromoDetails = promoDetailService.getByPromoID(promoID);

				// 針對這個promo，先把相關的bookID蒐集起來，以便資料庫batch操作
				removePromoDetails.forEach(pd -> {
					bookIDs.add(pd.getBookID());
				});
				// 找出這個promo相關的所有書
				List<Book> books = bookService.getByBookIDList(bookIDs);

				for (int i = 0; i < books.size(); i++) {
					Book book = books.get(i);
					String effectivePromos = book.getEffectivePromos();
					String removePromoID = promoID;

					// 刪除此促銷事件
					effectivePromos.replaceAll(removePromoID + ":[0-9]{2}:[0-9]{2},", ""); // 格式為promoID:兩位數字:兩位數字，用此regEx來刪除此字串
					// 若刪除後為空字串，則改為null，連同將促銷售價、促銷紅利設為NaN
					if ("".equals(effectivePromos) || effectivePromos == null) {
						effectivePromos = null;
						book.setSalePricePromo(Double.NaN);
						book.setBookBPPromo(Double.NaN);
					} else {
						// 仍有其他作用中的促銷事件
						int[] maxDiscountAndMaxBpPercent = { -1, 0 };

						String[] ohterPromos = effectivePromos.split(",");
						Map<String, int[]> otherPromoMap = new HashMap<String, int[]>();

						for (String op : ohterPromos) {
							int discount = Integer.parseInt(op.substring(op.indexOf(':') + 1, op.lastIndexOf(':')));
							int bpPercent = Integer.parseInt(op.substring(op.lastIndexOf(':') + 1));
							int[] discountAndBP = { discount, bpPercent };
							otherPromoMap.put(op, discountAndBP);
						}

						otherPromoMap.forEach((k, v) -> {
							if (v[0] > maxDiscountAndMaxBpPercent[0]) {
								maxDiscountAndMaxBpPercent[0] = v[0];
								maxDiscountAndMaxBpPercent[1] = v[1];
							}
						});

						// 取得maxDiscount之後再巡一次，避免有discount相同，但bpPercent更高的明細出現
						otherPromoMap.forEach((k, v) -> {
							if (v[0] == maxDiscountAndMaxBpPercent[0] && v[1] > maxDiscountAndMaxBpPercent[1]) {
								maxDiscountAndMaxBpPercent[1] = v[1];
							}
						});

						Double salePricePromo = calculateSalePricePromoOrBookBPPromo(book.getSalePrice(),
								(100 - maxDiscountAndMaxBpPercent[0]));
						Double bookBPPromo = calculateSalePricePromoOrBookBPPromo(book.getSalePrice(),
								maxDiscountAndMaxBpPercent[1]);

						book.setSalePricePromo(salePricePromo);
						book.setBookBPPromo(bookBPPromo);
					}
					book.setEffectivePromos(effectivePromos);
				}
				bookService.updateBooks(books);
			});
		}
	}

	private void newPromos(Set<Promo> newPromos) {
		if (newPromos.size() > 0) {
			System.out.println("\n生效的促銷事件: ");
			newPromos.forEach(promo -> {
				System.out.println(promo);
				String promoID = promo.getPromoID();
				List<String> bookIDs = new ArrayList<String>();
				List<PromoDetail> newPromoDetails = promoDetailService.getByPromoID(promoID);
				Map<String, PromoDetail> bookIDPDMap = new HashMap<String, PromoDetail>(); // bookID : PD

				// 針對這個promo，先把相關的bookID蒐集起來，以便資料庫batch操作
				newPromoDetails.forEach(pd -> {
					bookIDs.add(pd.getBookID());
					bookIDPDMap.put(pd.getBookID(), pd);
				});
				// 找出這個promo相關的所有書
				List<Book> books = bookService.getByBookIDList(bookIDs);

				for (int i = 0; i < books.size(); i++) {
					Book book = books.get(i);
					String effectivePromos = book.getEffectivePromos();
					PromoDetail pd = bookIDPDMap.get(book.getBookID());

					// 無其他作用中的促銷事件，直接設定促銷售價和促銷紅利
					if (effectivePromos == null) {
						effectivePromos = ""; // 先給空字串，加上"newPromoID,"之後為"newPromoID,"，而不會是"nullnewPromoID,"

						Double salePricePromo = calculateSalePricePromoOrBookBPPromo(book.getSalePrice(),
								(100 - pd.getDiscount()));
						Double bookBPPromo = calculateSalePricePromoOrBookBPPromo(book.getSalePrice(),
								pd.getBpPercent());

						if (salePricePromo < book.getSalePrice()) {
							book.setSalePricePromo(salePricePromo);
						}

						if (bookBPPromo > book.getBookBP()) {
							book.setBookBPPromo(bookBPPromo);
						}
					} else { // 仍有其他作用中的促銷事件
						// 仍有其他作用中的促銷事件
						int[] maxDiscountAndMaxBpPercent = { -1, 0 };

						String[] ohterPromos = effectivePromos.split(",");
						Map<String, int[]> otherPromoMap = new HashMap<String, int[]>();

						for (String op : ohterPromos) {
							int discount = Integer.parseInt(op.substring(op.indexOf(':') + 1, op.lastIndexOf(':')));
							int bpPercent = Integer.parseInt(op.substring(op.lastIndexOf(':') + 1));
							int[] discountAndBP = { discount, bpPercent };
							otherPromoMap.put(op, discountAndBP);
						}

						otherPromoMap.forEach((k, v) -> {
							if (v[0] > maxDiscountAndMaxBpPercent[0]) {
								maxDiscountAndMaxBpPercent[0] = v[0];
								maxDiscountAndMaxBpPercent[1] = v[1];
							}
						});

						// 取得maxDiscount之後再巡一次，避免有discount相同，但bpPercent更高的明細出現
						otherPromoMap.forEach((k, v) -> {
							if (v[0] == maxDiscountAndMaxBpPercent[0] && v[1] > maxDiscountAndMaxBpPercent[1]) {
								maxDiscountAndMaxBpPercent[1] = v[1];
							}
						});

						Double salePricePromo = calculateSalePricePromoOrBookBPPromo(book.getSalePrice(),
								(100 - maxDiscountAndMaxBpPercent[0]));
						Double bookBPPromo = calculateSalePricePromoOrBookBPPromo(book.getSalePrice(),
								maxDiscountAndMaxBpPercent[1]);

						book.setSalePricePromo(salePricePromo);
						book.setBookBPPromo(bookBPPromo);
					}

					StringBuffer effPromos = new StringBuffer(effectivePromos);
					String discountInThisPD = String.format(EFF_PROMO_FORMAT,
							bookIDPDMap.get(book.getBookID()).getDiscount());
					String bpPercentInThisPD = String.format(EFF_PROMO_FORMAT,
							bookIDPDMap.get(book.getBookID()).getBpPercent());
					// 格式為"promoID1:discount1兩位數字:bpPercent1兩位數字,promoID2:discount2兩位數字:bpPercent2兩位數字,"
					effPromos.append(promoID).append(':').append(discountInThisPD).append(':').append(bpPercentInThisPD)
							.append(',');
					book.setEffectivePromos(effPromos.toString());
				}
				bookService.updateBooks(books);
			});
		}
	}
}