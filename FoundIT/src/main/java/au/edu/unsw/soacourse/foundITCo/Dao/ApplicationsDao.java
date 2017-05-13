package au.edu.unsw.soacourse.foundITCo.Dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.tomcat.util.descriptor.web.ApplicationParameter;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

import au.edu.unsw.soacourse.foundITCo.Utils;
import au.edu.unsw.soacourse.foundITCo.beans.Application;
import au.edu.unsw.soacourse.foundITCo.beans.Keys;
import au.edu.unsw.soacourse.foundITCo.beans.Posting;

public class ApplicationsDao {

	// TODO change ip when deploy to docker
	private static final String JOB_URL = "http://localhost:8080/JobServices";
	private String shortKey;

	public ApplicationsDao(String shortKey) {
		super();
		this.shortKey = shortKey;
	}

	private void addKeys(WebClient client) {
		client.header(Keys.SECURITY_KEY, Keys.SECURITY_VAL);
		client.header(Keys.SHORT_KEY, shortKey);
	}

	public List<Application> findApplicationByPostingId(String id) {
		List<Application> list = new ArrayList<>();
		WebClient client = WebClient.create(JOB_URL, Arrays.asList(new JacksonJsonProvider()));
		client.path("/postings/" + id + "/applications");
		addKeys(client);
		list = (List<Application>) client.getCollection(Application.class);
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			Application application = (Application) iterator.next();
			System.out.println(application.getAppId());
		}
		return list;
	}

	public Application findApplicationById(String id) {
		WebClient client = WebClient.create(JOB_URL, Arrays.asList(new JacksonJsonProvider()));
		client.back(true);
		client.path("/applications/" + id);
		addKeys(client);
		try {
			Application a = client.get(Application.class);
			Utils.trasnfromApplicationStatus(a);
			return a;
		} catch (Exception e) {
			// TODO
			System.out.println(" this application id is not in the db of jobservices");
		}
		return null;
	}

	public Response createApplication(Application application) {
		WebClient client = WebClient.create(JOB_URL, Arrays.asList(new JacksonJsonProvider()));
		client.path("/applications");
		client.type(MediaType.APPLICATION_JSON);
		addKeys(client);
		client.post(application);
		Response serviceResponse = client.getResponse();
		return serviceResponse;
	}

	public Response updateStatus(String aid, String newStatus) {
		WebClient client = WebClient.create(JOB_URL, Arrays.asList(new JacksonJsonProvider()));
		client.path("/applications/" + newStatus + "/" + aid);
		addKeys(client);
		client.put(null);
		Response serviceResponse = client.getResponse();
		return serviceResponse;
	}
}
