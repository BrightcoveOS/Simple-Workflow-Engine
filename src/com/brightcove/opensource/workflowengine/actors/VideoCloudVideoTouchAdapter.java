package com.brightcove.opensource.workflowengine.actors;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.brightcove.commons.catalog.objects.Video;
import com.brightcove.commons.misc.logging.LogUtils;
import com.brightcove.mediaapi.exceptions.BrightcoveException;
import com.brightcove.mediaapi.wrapper.WriteApi;
import com.brightcove.opensource.workflowengine.Actor;
import com.brightcove.opensource.workflowengine.Workflow;
import com.brightcove.opensource.workflowengine.record.Property;
import com.brightcove.opensource.workflowengine.record.Record;

/**
 * <p>
 *    Takes each Record provided by downstream providers and uses it to
 *    "touch" a video in a Video Cloud account.  "Touching" a video simply
 *    means that the video will be updated (with new or old data) so that
 *    its last modified date changes.
 * </p>
 * 
 * <p>
 *    The following configuration properties will be honored:<ul>
 *        <li>video-id-prop - Property name on each record that stores the
 *            video id to be touched.</li>
 *        <li>video-reference-id-prop - Property name on each record that
 *            stores the video reference id to be touched.</li>
 *        <li>write-token - Write token for account.</li>
 *    </ul>
 * </p>
 * 
 * @author <a href="https://github.com/three4clavin">Sander Gates</a>
 *
 */
public class VideoCloudVideoTouchAdapter extends Actor {
	private String   videoIdName    = null;
	private String   videoRefIdName = null;
	private WriteApi writeApi       = null;
	private String   writeToken     = null;
	
	/**
	 * <p>
	 *    Default constructor - workflow this actor is part of must be
	 *    provided
	 * </p>
	 * 
	 * @param workflow Workflow this actor will be part of
	 */
	public VideoCloudVideoTouchAdapter(Workflow workflow){
		super(workflow);
		writeApi = new WriteApi();
	}
	
	/* (non-Javadoc)
	 * @see com.brightcove.opensource.workflowengine.Actor#run()
	 */
	public void run(){
		videoIdName    = getFirstPropertyValue("video-id-prop");
		videoRefIdName = getFirstPropertyValue("video-reference-id-prop");
		writeToken     = getFirstPropertyValue("write-token");
		
		if((videoIdName == null) && (videoRefIdName == null)){
			die("One or both of 'video-id-prop' or 'video-reference-id-prop' must be specified to the VideoCloudVideoToucher actor.");
		}
		
		if(writeToken == null){
			die("'write-token' must be specified as a property of the VideoCloudVideoToucher actor.");
		}
		
		if(! hasStarted()){
			List<String> apiRedactStrings = new ArrayList<String>();
			apiRedactStrings.add(writeToken);
			Logger apiLogger = LogUtils.getLogger("Brightcove Media API Wrapper", apiRedactStrings);
			writeApi = new WriteApi(apiLogger);
		}
		super.run();
	}
	
	/* (non-Javadoc)
	 * @see com.brightcove.opensource.workflowengine.Actor#handleRecord(com.brightcove.opensource.workflowengine.Record)
	 */
	public void handleRecord(Record record){
		Property<?> videoIdProp    = record.getFirstProperty(videoIdName);
		Property<?> videoRefIdProp = record.getFirstProperty(videoRefIdName);
		
		Video video = new Video();
		if((videoIdProp != null) && (videoIdProp.getValue() != null) && (! "".equals(videoIdProp.getValue()))){
			try{
				video.setId(Long.parseLong(""+videoIdProp.getValue()));
			}
			catch(Exception e){
				logError("Couldn't parse long from string '" + videoIdProp.getValue() + "'.");
				return;
			}
		}
		else if((videoRefIdProp != null) && (videoRefIdProp.getValue() != null) && (! "".equals(videoRefIdProp.getValue()))){
			video.setReferenceId(""+videoRefIdProp.getValue());
		}
		else{
			logError("Couldn't get ID or ReferenceId from record '" + record + "'.");
			return;
		}
		
		log("Updating video '" + video + "'.");
		
		try {
			writeApi.UpdateVideo(writeToken, video);
		}
		catch (BrightcoveException be) {
			logError("Couldn't update video (" + be + "): '" + video + "'.");
		}
	}
}
