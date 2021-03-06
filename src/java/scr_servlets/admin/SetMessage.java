package scr_servlets.admin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import scr.admin.Details;

public class SetMessage extends HttpServlet {

    private String filePath;

    @Override
    public void init() {
        filePath = getServletContext().getInitParameter("file-upload");  //to store uploaded data
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String message = null;
        HttpSession session = request.getSession(false);
        Details admin = (Details) session.getAttribute("ADMIN");
        try {
            boolean isMultipart = ServletFileUpload.isMultipartContent(request);
            if (isMultipart) {
                DiskFileItemFactory factory = new DiskFileItemFactory();
                factory.setSizeThreshold(5000000);
                factory.setRepository(new File("c:\\temp"));
                ServletFileUpload upload = new ServletFileUpload(factory);
                upload.setSizeMax(5000000);
                List items = upload.parseRequest(request);
                Iterator iter = items.iterator();
                ArrayList<FileItem> files = new ArrayList<>();
                while (iter.hasNext()) {
                    FileItem fi = (FileItem) iter.next();
                    if (fi.isFormField()) {
                        String fieldName = fi.getFieldName();
                        String fieldValue = fi.getString();
                        switch (fieldName) {
                            case "message":
                                message = fieldValue;
                                break;
                            default:
                                break;
                        }
                    } else {
                        String fieldName = fi.getFieldName();
                        if (fieldName.equals("userfile")) {
                            files.add(fi);
                        }
                    }
                }

                if (message != null && files.size() > 0) {
                    UUID uid = UUID.randomUUID();
                    String tid = String.valueOf(uid);
                    Date date;
                    int i = 1;
                    ArrayList<String> fnames = new ArrayList<>();
                    for (FileItem fi : files) {
                        String fileName = fi.getName();
                        File file;

                        date = new Date();
                        if (fileName.lastIndexOf("\\") >= 0) {
                            fileName = String.valueOf(date.getTime()) + "_" + i + "_" + fileName.substring(fileName.lastIndexOf("\\"));
                        } else {
                            fileName = String.valueOf(date.getTime()) + "_" + i + "_" + fileName.substring(fileName.lastIndexOf("\\") + 1);
                        }
                        i++;
                        fnames.add(fileName);
                        fi.write(new File(filePath + fileName));
                    }
                    admin.updateMarquee(tid, message, fnames);
                    response.sendRedirect("home");
                } else {
                    session.setAttribute("message", "Unable Set Marquee.");
                    response.sendRedirect("home");
                }
            } else {
                throw new ServletException("Invalid Request.");
            }
        } catch (Exception ex) {
            throw new ServletException(ex.getLocalizedMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("Login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

}
