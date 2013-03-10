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
import org.torweg.pulse.service.request.CacheMode;
import org.torweg.pulse.service.request.ServiceRequest;
import org.torweg.pulse.util.MimeMap;
import org.torweg.pulse.util.io.FastByteArrayOutputStream;
import org.torweg.pulse.util.io.SerializableDataSource;


public class UserListController extends Controller {
	
	private static final Logger LOGGER = LoggerFactory
						.getLogger(UserListController.class);
	
	@Action("downloadExcel")
	public final void downloadUsersExcel() {
		Session s = Lifecycle.getHibernateDataSource().createNewSession();
		Transaction tx = s.beginTransaction();
		try {

			@SuppressWarnings("unchecked")
			List<User> users = s.createCriteria(User.class).list();
			LOGGER.info("{} user(s) found", users.size());

			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			throw new PulseException("Error: " + e.getLocalizedMessage(), e);
		} finally {
			s.close();
		}
	}
}
