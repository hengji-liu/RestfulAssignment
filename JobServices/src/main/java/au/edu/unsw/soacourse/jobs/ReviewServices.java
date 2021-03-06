package au.edu.unsw.soacourse.jobs;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import au.edu.unsw.soacourse.jobs.auth.Roles;
import au.edu.unsw.soacourse.jobs.auth.RolesAllowed;
import au.edu.unsw.soacourse.jobs.auth.SecuredByKey;
import au.edu.unsw.soacourse.jobs.dao.ApplicationsDao;
import au.edu.unsw.soacourse.jobs.dao.PostingsDao;
import au.edu.unsw.soacourse.jobs.dao.ReviewsDao;
import au.edu.unsw.soacourse.jobs.model.Application;
import au.edu.unsw.soacourse.jobs.model.Posting;
import au.edu.unsw.soacourse.jobs.model.PostingStatus;
import au.edu.unsw.soacourse.jobs.model.Review;
import au.edu.unsw.soacourse.jobs.model.ReviewDecisoin;

@SecuredByKey
public class ReviewServices {
	private ReviewsDao rDao = new ReviewsDao();
	private PostingsDao pDao = new PostingsDao();
	private ApplicationsDao aDao = new ApplicationsDao();

	@GET
	@Path("/reviews/{rId}")
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@RolesAllowed({ Roles.C, Roles.M, Roles.R })
	public Response get(@HeaderParam("accept") String type, @PathParam("rId") String rId) {
		// validation, rId should be an int
		try {
			Integer.parseInt(rId);
		} catch (NumberFormatException e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		// get item
		Review r = rDao.findById(rId);
		if (null != r) {
			if (type.equals(MediaType.WILDCARD))
				type = MediaType.APPLICATION_JSON; // default json
			return Response.status(Status.OK).entity(r).type(type).build();
		} else {// item not found
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	@GET
	@Path("/reviews")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ Roles.C, Roles.M, Roles.R })
	public Response getAllReviews(@HeaderParam("accept") String type) {
		List<Review> list = rDao.findAll();
		if (null != list) {
			return Response.status(Status.OK).entity(list).type(MediaType.APPLICATION_JSON).build();
		} else {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GET
	@Path("applications/{appId}/reviews")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({ Roles.C, Roles.M, Roles.R })
	public Response getReviewByApp(@HeaderParam("accept") String type, @PathParam("appId") String appId) {
		// validation, appId is an valid int
		if (null != appId && !"".equals(appId)) {
			try {
				Integer.parseInt(appId);
			} catch (NumberFormatException e) {
				return Response.status(Status.BAD_REQUEST).build();
			}
		}
		// search on at least one param
		List<Review> list = rDao.findByAppId(appId);
		if (null != list) {
			return Response.status(Status.OK).entity(list).type(MediaType.APPLICATION_JSON).build();
		} else {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	@POST
	@Path("/reviews")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@RolesAllowed({ Roles.R })
	public Response post(Review obj) {
		// validation, reviewId must be null or empty
		String reviewId = obj.getReviewId();
		if (null != reviewId && !"".equals(reviewId))
			return Response.status(Status.BAD_REQUEST).build();
		// validation, appId must not be null and be an int
		String appId = obj.getAppId();
		if (null == appId)
			return Response.status(Status.BAD_REQUEST).build();
		try {
			Integer.parseInt(appId);
		} catch (NumberFormatException e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		// validation, other fields must not be null
		if (null == obj.getReviewerDetails()//
				|| null == obj.getComments()//
				|| null == obj.getDecision()) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		// validation decision must be 0/1
		String decision = obj.getDecision();
		try {
			Integer.parseInt(decision);
		} catch (NumberFormatException e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		if (ReviewDecisoin.NOT_RECOMMEND != Integer.parseInt(decision)
				&& ReviewDecisoin.RECOMMEND != Integer.parseInt(decision)) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		// check, posting status is in review
		Application a = aDao.findById(obj.getAppId());
		Posting p = pDao.findById(a.getJobId());
		if (PostingStatus.IN_REVIEW != Integer.parseInt(p.getStatus()))
			return Response.status(Status.BAD_REQUEST).entity("posting status is not in_review, can't post").build();
		// insert
		int insertedId = rDao.insert(obj);
		if (0 != insertedId) {
			URI uri = null;
			try {
				uri = new URI("review/" + Integer.toString(insertedId));
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			return Response.status(Status.CREATED).location(uri).build();
		}
		// insert fail
		return Response.status(Status.INTERNAL_SERVER_ERROR).build();
	}

	@PUT
	@Path("/reviews/{rId}")
	@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	@RolesAllowed({ Roles.R })
	public Response put(@PathParam("rId") String rId, Review obj) {
		// validation, rId should be an int
		try {
			Integer.parseInt(rId);
		} catch (NumberFormatException e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
		// validation, rId in payload must be null or empty
		String rIdPayload = obj.getReviewId();
		if (null != rIdPayload)
			return Response.status(Status.BAD_REQUEST).build();
		// validation, has something to update
		boolean hasUpdate = false;
		hasUpdate |= (null != obj.getReviewerDetails());
		hasUpdate |= (null != obj.getComments());
		hasUpdate |= (null != obj.getDecision());
		hasUpdate |= (null != obj.getAppId());
		if (!hasUpdate)
			return Response.status(Status.BAD_REQUEST).build();
		// check item exists
		Review r = rDao.findById(rId);
		if (null == r)
			return Response.status(Status.NOT_FOUND).build();
		// check, posting status is in review
		Application a = aDao.findById(obj.getAppId());
		Posting p = pDao.findById(a.getJobId());
		if (PostingStatus.IN_REVIEW != Integer.parseInt(p.getStatus()))
			return Response.status(Status.BAD_REQUEST).entity("posting status is not in_review, can't update").build();
		// update
		obj.setReviewId(rId);
		int affectedRowCount = rDao.update(obj);
		if (0 == affectedRowCount) { // update fail
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		} else {
			return Response.status(Status.NO_CONTENT).build();
		}
	}
}
