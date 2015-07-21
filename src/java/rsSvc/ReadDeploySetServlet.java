/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rsSvc;

import common.DeployInfo;
import common.RSThreadPool;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import webSocket.AssignTrial;

/**
 *
 * @author Administrator
 */
public class ReadDeploySetServlet extends HttpServlet {

    public ReadDeploySetServlet() {
        try {
            if (!DeployInfo.readSetUp()) {
                throw new Exception("load deployInfo failed,check log. " + DeployInfo.DeployRootPath + ".");
            }
        } catch (Exception e) {
            common.RSLogger.SetUpLogInfo(String.format("start service error step readSetUp ,%s", e.getLocalizedMessage()));
            common.RSLogger.ErrorLogInfo(String.format("start service error step readSetUp ,%s", e.getLocalizedMessage()), e);
        }
        try {
            RSThreadPool.initialTheadPool();
        } catch (Exception e) {
            common.RSLogger.SetUpLogInfo(String.format("start service error step initialCachedTheadPool ,%s", e.getLocalizedMessage()));
            common.RSLogger.ErrorLogInfo(String.format("start service error step initialCachedTheadPool ,%s", e.getLocalizedMessage()), e);
        }
        try {
            common.RSLogger.Initial();
        } catch (Exception e) {
            common.RSLogger.SetUpLogInfo(String.format("start service error step RSLogger.Initial ,%s", e.getLocalizedMessage()));
            common.RSLogger.ErrorLogInfo(String.format("start service error step RSLogger.Initial ,%s", e.getLocalizedMessage()), e);
        }
        try {
            if (!common.DBHelper.initializePool()) {
                throw new Exception("db initializePool error .");
            }
        } catch (Exception e) {
            common.RSLogger.SetUpLogInfo(String.format("start service error step initializePool,%s", e.getLocalizedMessage()));
            common.RSLogger.ErrorLogInfo(String.format("start service error step initializePool,%s", e.getLocalizedMessage()), e);
        }
        try {
            if (!common.DBHelper.LoadDBInfo()) {
                throw new Exception("readDBInformation error.");
            }
        } catch (Exception e) {
            common.RSLogger.SetUpLogInfo(String.format("start service error step readDBInformation,%s", e.getLocalizedMessage()));
            common.RSLogger.ErrorLogInfo(String.format("start service error step readDBInformation,%s", e.getLocalizedMessage()), e);
        }
        try {
            AssignTrial.initialWebSocketService();
        } catch (Exception e) {
            common.RSLogger.SetUpLogInfo(String.format("start service error step initialWebSocketService,%s", e.getLocalizedMessage()));
            common.RSLogger.ErrorLogInfo(String.format("start service error step initialWebSocketService,%s", e.getLocalizedMessage()), e);
        }

    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, Exception {
        try {
            if (!common.DBHelper.LoadDBInfo()) {
                throw new Exception("readDBInformation error.");
            }
            response.getWriter().print("reload the database information to complete .");
        } catch (Exception e) {
            common.RSLogger.SetUpLogInfo(String.format("reLoad db information error step readDBInformation,%s", e.getLocalizedMessage()));
            common.RSLogger.ErrorLogInfo(String.format("reLoad db information error step readDBInformation,%s", e.getLocalizedMessage()), e);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            processRequest(request, response);
        } catch (Exception ex) {
            throw new ServletException("load deployInfo failed,check log. " + DeployInfo.DeployRootPath + "." + ex.getLocalizedMessage());
        }

    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            throw new ServletException("load deployInfo failed,check log. " + DeployInfo.DeployRootPath + "." + ex.getLocalizedMessage());
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
