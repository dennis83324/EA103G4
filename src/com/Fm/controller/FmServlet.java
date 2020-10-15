package com.Fm.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.Fa.model.FaService;
import com.Fa.model.FaVO;
import com.Fm.model.*;

public class FmServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		doPost(req, res);
	}

	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		req.setCharacterEncoding("UTF-8");
		String action = req.getParameter("action");
		// 新增留言
		if("insert".equals(action)) {
			List<String> errorMsgs = new LinkedList<String>();
			req.setAttribute("errorMsgs", errorMsgs);
			
			try {
				
				String faId = req.getParameter("faId");
				
				String memId = req.getParameter("memId");
				
				String fmContent = req.getParameter("fmContent");
				if(fmContent == null || fmContent.trim().length() == 0) {
					errorMsgs.add("留言請勿空白");
				}
				
				FmVO fmVO = new FmVO();
				fmVO.setFaId(faId);
				fmVO.setMemId(memId);
				fmVO.setFmContent(fmContent);
				
				FaVO faVO = new FaVO();
				List<FmVO> list = new ArrayList<>();  
				
				if(!errorMsgs.isEmpty()) {
					req.setAttribute("fmVO", fmVO);
					RequestDispatcher failureView = req
							.getRequestDispatcher("/front-end/forum/addFmPage.jsp");
					failureView.forward(req, res);
					return;
				}
				
				FmService fmSvc = new FmService();
				fmVO = fmSvc.addFm(faId, memId, fmContent);
				list = fmSvc.getOneFAFm(faId);
				
				FaService faSvc = new FaService();
				faVO = faSvc.getOneFa(faId);
				
				
				req.setAttribute("faVO",faVO);
				req.setAttribute("list",list);
				
				String url = "/front-end/forum/forumPage.jsp";
				RequestDispatcher successView = req.getRequestDispatcher(url);
				successView.forward(req, res);
				
			}catch(Exception e){
				errorMsgs.add(e.getMessage());
				RequestDispatcher failureView = req
						.getRequestDispatcher("/front-end/forum/addFmPage.jsp");
				failureView.forward(req, res);
			}			
		}
		//修改留言
		if("update".equals(action)) {
			List<String> errorMsgs = new LinkedList<String>();
			req.setAttribute("errorMsgs", errorMsgs);
			
			try {
				String fmId = new String(req.getParameter("fmId"));
				String fmContent = req.getParameter("fmContent"); 
				if(fmContent == null || fmContent.trim().length() == 0) {
					errorMsgs.add("內容請勿空白");
				}
				
				FmVO fmVO = new FmVO();
				
				fmVO.setFmId(fmId);
				fmVO.setFmContent(fmContent);
				
				if (!errorMsgs.isEmpty()) {
					req.setAttribute("fmVO", fmVO);
					RequestDispatcher failureView = req.getRequestDispatcher("");
					failureView.forward(req, res);
					return;
				}
				/*************************** 2.修改資料 ***************************************/
				FmService fmSvc = new FmService();
				fmVO = fmSvc.updateFm(fmId, fmContent);
				
				req.setAttribute("fmVO", fmVO);
				String url = "";
				RequestDispatcher successView = req.getRequestDispatcher(url);
				successView.forward(req, res);
			} catch (Exception e) {
				errorMsgs.add(e.getMessage());
				RequestDispatcher failureView = req.getRequestDispatcher("");
				failureView.forward(req, res);
			}
			
		}
		//轉交到新增留言頁面
		if("getOne_For_AddFm".equals(action)) {
			List<String> errorMsgs = new LinkedList<String>();
			req.setAttribute("errorMsgs", errorMsgs);
			
			try {
				String faId =req.getParameter("faId");
	
				String memId = req.getParameter("memId");

				FaVO faVO = new FaVO();
				faVO.setFaId(faId);
				faVO.setMemId(memId);
				
				req.setAttribute("faVO", faVO);
				String url = "/front-end/forum/addFmPage.jsp";
				RequestDispatcher successView = req.getRequestDispatcher(url);
				successView.forward(req, res);
				
			} catch (Exception e) {
				errorMsgs.add("無法取得要修改的資料:" + e.getMessage());
				RequestDispatcher failureView = req
						.getRequestDispatcher("/front-end/forum/addFmPage.jsp");
				failureView.forward(req, res);
			}

		}
		
		//轉交到修改留言頁面
		if("getOne_For_UpdateFm".equals(action)) {
			List<String> errorMsgs = new LinkedList<String>();
			req.setAttribute("errorMsgs", errorMsgs);
			
			try {
				String fmId = req.getParameter("fmId");
				String fmContent = req.getParameter("fmContent");
				
				FmVO fmVO = new FmVO();
				fmVO.setFmId(fmId);
				fmVO.setFmContent(fmContent);
				
				req.setAttribute("fmVO", fmVO);
				String url = "/fmUpdatePage.jsp";
				RequestDispatcher successView = req.getRequestDispatcher(url);
				successView.forward(req,res);
			} catch (Exception e) {
				errorMsgs.add("無法取得要修改的資料:" + e.getMessage());
				RequestDispatcher failureView = req
						.getRequestDispatcher("/fmUpdatePage.jsp");
				failureView.forward(req, res);
			}
			
			
			
		}
		//下架留言
		if("logout".equals(action)) {
			List<String> errorMsgs = new LinkedList<String>();
			req.setAttribute("errorMsgs", errorMsgs);
			
			try {
				String fmId = req.getParameter("fmId");
				
				FmService fmSvc = new FmService();
				fmSvc.logout_Fm(fmId);
				
				String url = "/forumIndex.jsp";
				RequestDispatcher successView = req.getRequestDispatcher(url);
				successView.forward(req,res);
			} catch (Exception e) {
				errorMsgs.add("無法取得要修改的資料:" + e.getMessage());
				RequestDispatcher failureView = req
						.getRequestDispatcher("/forumIndex.jsp");
				failureView.forward(req, res);
			}
			
			
		}
		
	
	}

}
