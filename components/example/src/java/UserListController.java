package org.torweg.pulse.component.example;

import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.torweg.pulse.accesscontrol.User;
import org.torweg.pulse.annotations.Action;
import org.torweg.pulse.bundle.Controller;
import org.torweg.pulse.invocation.lifecycle.Lifecycle;
import org.torweg.pulse.service.PulseException;
import org.torweg.pulse.service.event.DownloadEvent;
import org.torweg.pulse.service.event.Event;
import org.torweg.pulse.service.event.EventManager;
//import org.torweg.pulse.service.event.Event.Disposition; //Added line
import org.torweg.pulse.service.request.CacheMode;
import org.torweg.pulse.service.request.ServiceRequest;
import org.torweg.pulse.util.MimeMap;
import org.torweg.pulse.util.io.FastByteArrayOutputStream;
import org.torweg.pulse.util.io.SerializableDataSource;


public class UserListController extends Controller {
	
	private static final Logger LOGGER = LoggerFactory
						.getLogger(UserListController.class);
	
	@Action("downloadExcel")
	public final void downloadUsersExcel(final ServiceRequest request) {
		Session s = Lifecycle.getHibernateDataSource().createNewSession();
		Transaction tx = s.beginTransaction();
		try {

			@SuppressWarnings("unchecked")
			List<User> users = s.createCriteria(User.class).list();
			LOGGER.info("{} user(s) found", users.size());
			
			//create empty Excel workbook
			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet("users");

			for(User user :users) {
				Row row = sheet.createRow(sheet.getLastRowNum() + 1);
				row.createCell(row.getLastCellNum() + 1).setCellValue(
								user.getId().toString());
				row.createCell(row.getLastCellNum() + 1).setCellValue(
								user.getName());
				row.createCell(row.getLastCellNum() + 1).setCellValue(
								user.getEmail());
				if(user.getLastLoginTime() != null) {
					row.createCell(row.getLastCellNum() + 1).setCellValue(
									user.getLastLoginTime().toString());
				}	
			}

			//write workbook to buffer
			FastByteArrayOutputStream buffer = new FastByteArrayOutputStream();
			workbook.write(buffer);
			
			//wrap buffer into a SerializableDataSource
			String filename = "users.xlsx";
			SerializableDataSource dataSource = SerializableDataSource.fromByteArray(buffer.getByteArray());
			dataSource.setName(filename).setContentType(MimeMap.getMimeType(filename));
			//dataSource;

			//create a DownloadEvent
			DownloadEvent downloadEvent = new DownloadEvent(dataSource,
							Event.Disposition.ATTACHMENT, CacheMode.NONE); //changed to Attachment

			//add event to EventManager
			EventManager eventManager = request.getEventManager();
			eventManager.addEvent(downloadEvent);

			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			throw new PulseException("Error: " + e.getLocalizedMessage(), e);
		} finally {
			s.close();
		}
	}
}
