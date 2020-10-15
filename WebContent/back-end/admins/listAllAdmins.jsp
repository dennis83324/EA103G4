<%@ page contentType="text/html; charset=UTF-8" pageEncoding="Big5"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ page import="java.util.*"%>
<%@ page import="com.admins.model.*"%>
<%@ page import="com.adminpermission.model.*"%>
<%@ page import="com.permissiondelimit.model.*"%>

<%
response.setHeader("Cache-Control","no-cache, no-store, must-revalidate"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires", 0); //prevent caching at the proxy server

if(session.getAttribute("admin_id") == null){
  response.sendRedirect(request.getContextPath() + "/back-end/login/login.jsp");
}

%>
<%
  AdminsService adminsSvc = new AdminsService();
  List<AdminsVO> list = adminsSvc.getAll();
  pageContext.setAttribute("list", list);
%>
<%
	List<AdminPermissionVO> adminpermissionVO = (List<AdminPermissionVO>) request
			.getAttribute("adminpermissionVO"); //用集合來接許多權限的物件
%>

<%
  AdminsVO adminsVO = (AdminsVO) request.getAttribute("adminsVO"); //用集合來接許多權限的物件
%>


<%
	List<PermissionDelimitVO> permissiondelimitVO = (List<PermissionDelimitVO>) request
			.getAttribute("permissiondelimitVO"); //用集合來接許多權限的物件
%>
<!DOCTYPE html>
<html>

<head>
    
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <meta name="description" content="">
    <meta name="author" content="">
    <meta http-equiv="Pragma" content="no-cache">
    <meta http-equiv="Cache-Control" content="no-cache">
    <meta http-equiv="Expires" content="0">
    <title>員工列表管理</title>
        
    <!-- BOOSTRAP CSS -->
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/vendors/styles/core.css">
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/vendors/styles/icon-font.min.css">
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/vendors/styles/style.css">
  
    <!-- 自訂CSS -->
    <style type="text/css">
        .main-container {
            padding-left: 270px;
            padding-top: 80px;
            padding-right: 30px;
        }
    </style>
</head>


<body>
<jsp:include page="/back-end/header/header.jsp" />
    <div class="main-container">
        <main>
        <div class="pd-20 card-box mb-30">
            <div class="clearfix mb-20">
                <div class="pull-left">
                    <h1 class="text-blue h1">員工列表</h1>
                    <div>
                        <a href="<%=request.getContextPath()%>/back-end/admins/addAdmins.jsp"><img type="button" id="button" src="<%=request.getContextPath()%>/images/admins/user_add.png" height="30px" width="30px" align="left"></a>
                    </div>
                </div>
                <jsp:useBean id="adminsSvc1" scope="page" class="com.admins.model.AdminsService" />
                <div class="pull-right">
                    <form method="post" action="<%=request.getContextPath()%>/back-end/admins/admins.do" class="d-none d-sm-inline-block form-inline mr-auto ml-md-3 my-2 my-md-0 mw-100 navbar-search">
                        <div class="input-group">
                            <select class="custom-select form-control" name="admin_id">
                                <c:forEach var="adminsVO" items="${adminsSvc1.all}">
                                    <option value="${adminsVO.admin_id}">${adminsVO.admin_id}
                                        ${adminsVO.admin_name}
                                </c:forEach>
                            </select>
                            <div class="input-group-append">
                                <input type="hidden" name="action" value="getOne_For_Display">
                                <input type="submit" value="送出" class="btn btn-dark btn-sm">
                            </div>
                        </div>
                    </form>
                </div>
            </div>
            <table class="table">
                <thead class="thead-dark">
                    <tr>
                    
                        <th scope="col">編號</th>
                        <th scope="col">照片</th>
                        <th scope="col">姓名</th>
                        <th scope="col">郵件</th>
                        <th scope="col">狀態</th>
                        <th scope="col">異動</th>
                        <th scope="col"></th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="adminsVO" items="${list}">
                        <tr>
                            <td>${adminsVO.admin_id}</td>
                            <td>
                                <div id="img">
                                    <img width="80px" heigh="80px" src="${pageContext.request.contextPath}/ShowAdminPic?admin_id=${adminsVO.admin_id}" />
                                </div>
                            </td>
                            <td>${adminsVO.admin_name}</td>
                            <td>${adminsVO.admin_mail}</td>
                            <td>${adminsVO.admin_jobstate == 1?"在職":"離職"}</td>
                            </td>
                            <td>
                            
                                <form method="post" action="<%=request.getContextPath()%>/back-end/admins/admins.do">
                                    <input type="image" name="submitPng" src="<%=request.getContextPath()%>/images/admins/edit.png" onclick="submit()" border="0">
                                    <input type="hidden" name="admin_id" value="${adminsVO.admin_id}">
                                    <input type="hidden" name="admin_id" value="${adminpermissionVO.admin_id}">
                                    <input type="hidden" name="action" value="getOne_For_Update">
                                </form>
                            </td>
                            <td>
                                <form class="pull-left" method="post" action="<%=request.getContextPath()%>/back-end/admins/admins.do">
                                    <input type="image" name="viewPng" src="<%=request.getContextPath()%>/images/admins/view.png" onclick="submit()" border="0">
                                    <input type="hidden" name="admin_id" value="${adminsVO.admin_id}">
                                    <input type="hidden" name="admin_id" value="${adminpermissionVO.admin_id}">
                                    <input type="hidden" name="action" value="getOne_For_Display">
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
        
        </main>
    </div>
    <jsp:include page="/back-end/sidebar/sidebar.jsp" />
   <div class="form-group h3 text-dark">
												
</body>
</html>