package com.signup.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.signup.model.*;

public class SignupDAO implements SignupDAO_interface {
	// �@�����ε{����,�w��@�Ӹ�Ʈw ,�@�Τ@��DataSource�Y�i
	private static DataSource ds = null;
	static {
		try {
			Context ctx = new InitialContext();
			ds = (DataSource) ctx.lookup("java:comp/env/jdbc/bookshop");
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	private static final String INSERT_STMT = "INSERT INTO SIGNUP_DETAIL (SIGNUP_ID, MEM_ID, LC_ID, SIGNUP_PAY, PAY_STATE, SIGNUP_TIME, PAY_TIME, SIGN_SEAT) VALUES ('S' || lpad(SIGNUP_SEQ.NEXTVAL, 4, '0'), ?, ?, ?, ?, DEFAULT, ?, ?)";
	private static final String GET_ALL_STMT = "SELECT * FROM SIGNUP_DETAIL order by SIGNUP_ID";
	private static final String GET_ONE_STMT = "SELECT * FROM SIGNUP_DETAIL WHERE SIGNUP_ID = ?";
	private static final String GET_LECTURE_STMT = "SELECT * FROM SIGNUP_DETAIL WHERE LC_ID = ?";
	private static final String GET_MEMBER_STMT = "SELECT * FROM SIGNUP_DETAIL WHERE MEM_ID = ?";
	private static final String DELETE = "DELETE FROM SIGNUP_DETAIL where SIGNUP_ID = ?";
	private static final String UPDATE = "UPDATE SIGNUP_DETAIL set SIGNUP_PAY=?, PAY_STATE=?, PAY_TIME=?, SIGN_SEAT=? WHERE SIGNUP_ID = ?";

	@Override
	public void insert(SignupVO signupVO) {

		Connection con = null;
		PreparedStatement pstmt = null;

		try {

			con = ds.getConnection();
			pstmt = con.prepareStatement(INSERT_STMT);

			pstmt.setString(1, signupVO.getMem_id());
			pstmt.setString(2, signupVO.getLc_id());
			pstmt.setString(3, signupVO.getSignup_pay());
			pstmt.setInt(4, signupVO.getPay_state());
//			pstmt.setTimestamp(5, signupVO.getSignup_time());
			pstmt.setTimestamp(5, signupVO.getPay_time());
			pstmt.setString(6, signupVO.getSign_seat());

			pstmt.executeUpdate();

			// Handle any SQL errors
		} catch (SQLException se) {
			throw new RuntimeException("A database error occured. " + se.getMessage());
			// Clean up JDBC resources
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException se) {
					se.printStackTrace(System.err);
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}
		}

	}

	@Override
	public void update(SignupVO signupVO) {

		Connection con = null;
		PreparedStatement pstmt = null;

		try {

			con = ds.getConnection();
			pstmt = con.prepareStatement(UPDATE);

			pstmt.setString(1, signupVO.getSignup_pay());
			pstmt.setInt(2, signupVO.getPay_state());
			pstmt.setTimestamp(3, signupVO.getPay_time());
			pstmt.setString(4, signupVO.getSign_seat());

			pstmt.executeUpdate();

			// Handle any driver errors
		} catch (SQLException se) {
			throw new RuntimeException("A database error occured. " + se.getMessage());
			// Clean up JDBC resources
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException se) {
					se.printStackTrace(System.err);
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}
		}

	}

	@Override
	public void delete(String signup_id) {

		Connection con = null;
		PreparedStatement pstmt = null;

		try {

			con = ds.getConnection();
			pstmt = con.prepareStatement(DELETE);

			pstmt.setString(1, signup_id);

			pstmt.executeUpdate();

			// Handle any driver errors
		} catch (SQLException se) {
			throw new RuntimeException("A database error occured. " + se.getMessage());
			// Clean up JDBC resources
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException se) {
					se.printStackTrace(System.err);
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}
		}

	}

	@Override
	public SignupVO findByPrimaryKey(String signup_id) {

		SignupVO signupVO = null;
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {

			con = ds.getConnection();
			pstmt = con.prepareStatement(GET_ONE_STMT);

			pstmt.setString(1, signup_id);

			rs = pstmt.executeQuery();

			while (rs.next()) {
				// signupVO �]�٬� Domain objects
				signupVO = new SignupVO();
				signupVO.setSignup_id(rs.getString("signup_id"));
				signupVO.setMem_id(rs.getString("mem_id"));
				signupVO.setLc_id(rs.getString("lc_id"));
				signupVO.setSignup_pay(rs.getString("signup_pay"));
				signupVO.setPay_state(rs.getInt("pay_state"));
				signupVO.setSignup_time(rs.getTimestamp("signup_time"));
				signupVO.setPay_time(rs.getTimestamp("pay_time"));
				signupVO.setSign_seat(rs.getString("sign_seat"));
			}

			// Handle any driver errors
		} catch (SQLException se) {
			throw new RuntimeException("A database error occured. " + se.getMessage());
			// Clean up JDBC resources
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException se) {
					se.printStackTrace(System.err);
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException se) {
					se.printStackTrace(System.err);
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}
		}
		return signupVO;
	}

	@Override
	public List<SignupVO> findByLecture(String signup_id) {
		
		List<SignupVO> list1 = new ArrayList<SignupVO>();
		SignupVO signupVO = null;
		
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {

			con = ds.getConnection();
			pstmt = con.prepareStatement(GET_LECTURE_STMT);

			pstmt.setString(1, signup_id);

			rs = pstmt.executeQuery();

			while (rs.next()) {
				// signupVO �]�٬� Domain objects
				signupVO = new SignupVO();
				signupVO.setSignup_id(rs.getString("signup_id"));
				signupVO.setMem_id(rs.getString("mem_id"));
				signupVO.setLc_id(rs.getString("lc_id"));
				signupVO.setSignup_pay(rs.getString("signup_pay"));
				signupVO.setPay_state(rs.getInt("pay_state"));
				signupVO.setSignup_time(rs.getTimestamp("signup_time"));
				signupVO.setPay_time(rs.getTimestamp("pay_time"));
				signupVO.setSign_seat(rs.getString("sign_seat"));
				list1.add(signupVO); // Store the row in the list
			}

			// Handle any driver errors
		} catch (SQLException se) {
			throw new RuntimeException("A database error occured. " + se.getMessage());
			// Clean up JDBC resources
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException se) {
					se.printStackTrace(System.err);
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException se) {
					se.printStackTrace(System.err);
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}
		}
		return list1;
	}

	@Override
	public List<SignupVO> findByMember(String signup_id) {
		
		List<SignupVO> list2 = new ArrayList<SignupVO>();
		SignupVO signupVO = null;
		
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {

			con = ds.getConnection();
			pstmt = con.prepareStatement(GET_MEMBER_STMT);

			pstmt.setString(1, signup_id);

			rs = pstmt.executeQuery();

			while (rs.next()) {
				// signupVO �]�٬� Domain objects
				signupVO = new SignupVO();
				signupVO.setSignup_id(rs.getString("signup_id"));
				signupVO.setMem_id(rs.getString("mem_id"));
				signupVO.setLc_id(rs.getString("lc_id"));
				signupVO.setSignup_pay(rs.getString("signup_pay"));
				signupVO.setPay_state(rs.getInt("pay_state"));
				signupVO.setSignup_time(rs.getTimestamp("signup_time"));
				signupVO.setPay_time(rs.getTimestamp("pay_time"));
				signupVO.setSign_seat(rs.getString("sign_seat"));
				list2.add(signupVO); // Store the row in the list
			}

			// Handle any driver errors
		} catch (SQLException se) {
			throw new RuntimeException("A database error occured. " + se.getMessage());
			// Clean up JDBC resources
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException se) {
					se.printStackTrace(System.err);
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException se) {
					se.printStackTrace(System.err);
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}
		}
		return list2;
	}

	@Override
	public List<SignupVO> getAll() {
		List<SignupVO> list3 = new ArrayList<SignupVO>();
		SignupVO signupVO = null;

		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {

			con = ds.getConnection();
			pstmt = con.prepareStatement(GET_ALL_STMT);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				// signupVO �]�٬� Domain objects
				signupVO = new SignupVO();
				signupVO.setSignup_id(rs.getString("signup_id"));
				signupVO.setMem_id(rs.getString("mem_id"));
				signupVO.setLc_id(rs.getString("lc_id"));
				signupVO.setSignup_pay(rs.getString("signup_pay"));
				signupVO.setPay_state(rs.getInt("pay_state"));
				signupVO.setSignup_time(rs.getTimestamp("signup_time"));
				signupVO.setPay_time(rs.getTimestamp("pay_time"));
				signupVO.setSign_seat(rs.getString("sign_seat"));
				list3.add(signupVO); // Store the row in the list
			}

			// Handle any driver errors
		} catch (SQLException se) {
			throw new RuntimeException("A database error occured. " + se.getMessage());
			// Clean up JDBC resources
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException se) {
					se.printStackTrace(System.err);
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException se) {
					se.printStackTrace(System.err);
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}
		}
		return list3;
	}

}
