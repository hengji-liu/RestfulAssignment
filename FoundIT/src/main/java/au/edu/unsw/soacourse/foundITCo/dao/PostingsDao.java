package au.edu.unsw.soacourse.foundITCo.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

import au.edu.unsw.soacourse.foundITCo.Keys;
import au.edu.unsw.soacourse.foundITCo.Utils;
import au.edu.unsw.soacourse.foundITCo.beans.Posting;

public class PostingsDao {

	// TODO change ip when deploy to docker
	private static final String JOB_URL = "http://localhost:8080/JobServices";
	private String shortKey;

	public PostingsDao(String shortKey) {
		super();
		this.shortKey = shortKey;
	}

	private void addKeys(WebClient client) {
		client.header(Keys.SECURITY_KEY, Keys.SECURITY_VAL);
		client.header(Keys.SHORT_KEY, shortKey);
	}

	public Posting findPostingById(String id) {
		WebClient client = WebClient.create(JOB_URL, Arrays.asList(new JacksonJsonProvider()));
		client.back(true);
		client.path("/postings/" + id);
		addKeys(client);
		try {
			Posting p = client.get(Posting.class);
			Utils.trasnfromPostingStatus(p);
			return p;
		} catch (Exception e) {
			// TODO
			System.out.println(" this posting id is not in the db of jobservices");
		}
		return null;
	}

	public Response createPosting(Posting posting) {
		WebClient client = WebClient.create(JOB_URL, Arrays.asList(new JacksonJsonProvider()));
		client.path("/postings");
		client.type(MediaType.APPLICATION_JSON);
		addKeys(client);
		client.post(posting);
		Response serviceResponse = client.getResponse();
		return serviceResponse;
	}

	public Response updateStatus(String pid, String newStatus) {
		WebClient client = WebClient.create(JOB_URL, Arrays.asList(new JacksonJsonProvider()));
		client.path("/postings/" + newStatus + "/" + pid);
		addKeys(client);
		client.put(null);
		Response serviceResponse = client.getResponse();
		return serviceResponse;
	}

	public List<Posting> search(String keyword, String status) {
		List<Posting> list = new ArrayList<>();
		WebClient client = WebClient.create(JOB_URL, Arrays.asList(new JacksonJsonProvider()));
		client.path("/postings").query("keyword", keyword).query("status", status);
		addKeys(client);
		list.addAll(client.getCollection(Posting.class));
		for (Iterator<?> iterator = list.iterator(); iterator.hasNext();) {
			Posting posting = (Posting) iterator.next();
			Utils.trasnfromPostingStatus(posting);
		}
		return list;
	}

	public Response updatePosting(String id, Posting posting) {
		WebClient client = WebClient.create(JOB_URL, Arrays.asList(new JacksonJsonProvider()));
		client.path("/postings/" + id);
		client.type(MediaType.APPLICATION_JSON);
		addKeys(client);
		client.put(posting);
		Response serviceResponse = client.getResponse();
		return serviceResponse;
	}

}

