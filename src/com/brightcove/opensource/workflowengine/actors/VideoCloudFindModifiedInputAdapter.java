package com.brightcove.opensource.workflowengine.actors;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import com.brightcove.commons.catalog.objects.Video;
import com.brightcove.commons.catalog.objects.Videos;
import com.brightcove.commons.catalog.objects.enumerations.SortByTypeEnum;
import com.brightcove.commons.catalog.objects.enumerations.SortOrderTypeEnum;
import com.brightcove.commons.catalog.objects.enumerations.VideoFieldEnum;
import com.brightcove.commons.catalog.objects.enumerations.VideoStateFilterEnum;
import com.brightcove.commons.collection.CollectionUtils;
import com.brightcove.commons.misc.logging.LogUtils;
import com.brightcove.mediaapi.exceptions.BrightcoveException;
import com.brightcove.mediaapi.wrapper.ReadApi;
import com.brightcove.opensource.workflowengine.Actor;
import com.brightcove.opensource.workflowengine.Workflow;
import com.brightcove.opensource.workflowengine.record.Property;
import com.brightcove.opensource.workflowengine.record.Record;

/**
 * <p>
 *    An input adapter that will read from Brightcove Video Cloud Media API
 *    using the find_modified_videos method.
 * </p>
 * 
 * <p>
 *    The following configuration properties will be honored:<ul>
 *        <li>read-api-token - Media API token to use the Read API</li>
 *        <li>video-fields - Comma separated list of video fields to retrieve</li>
 *        <li>custom-fields - Comma separated list of custom fields to retrieve</li>
 *        <li>video-state-filter - Commas separated list of item states valid to search for</li>
 *        <li>from-date - Earliest video to retrieve (based on last modified date).  Format yyyy/MM/dd HH:mm:ss z</li>
 *    </ul>
 * </p>
 * 
 * @author <a href="https://github.com/three4clavin">Sander Gates</a>
 *
 */
public class VideoCloudFindModifiedInputAdapter extends Actor {
	private String                    readApiToken     = null;
	private ReadApi                   readApi          = null;
	private EnumSet<VideoFieldEnum>   videoFields      = null;
	private Set<String>               customFields     = null;
	private Set<VideoStateFilterEnum> videoStateFilter = null;
	private Integer                   pageSize         = null;
	private Integer                   pageNumber       = null;
	private SortByTypeEnum            sortBy           = null;
	private SortOrderTypeEnum         sortOrderType	   = null;
	private Long                      fromDate         = null;
	
	private String           dateFormat    = "yyyy/MM/dd HH:mm:ss z";
	private SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
	
	/**
	 * <p>
	 *    Default constructor - workflow this actor is part of must be
	 *    provided
	 * </p>
	 * 
	 * @param workflow Workflow this actor will be part of
	 */
	public VideoCloudFindModifiedInputAdapter(Workflow workflow){
		super(workflow);
	}
	
	/* (non-Javadoc)
	 * @see com.brightcove.opensource.workflowengine.Actor#run()
	 */
	public void run(){
		if(! hasStarted()){
			Property<String> readApiTokenProp = getFirstProperty("read-api-token");
			Property<String> videoFieldsProp  = getFirstProperty("video-fields");
			Property<String> customFieldsProp = getFirstProperty("custom-fields");
			Property<String> videoStateProp   = getFirstProperty("video-state-filter");
			Property<String> fromDateProp     = getFirstProperty("from-date");
			
			if((readApiTokenProp == null) || (readApiTokenProp.getValue() == null)){
				die("VideoCloudFindModifiedInputAdapter requires a read-api-token property.");
			}
			readApiToken = readApiTokenProp.getValue();
			
			List<String> apiRedactStrings = new ArrayList<String>();
			apiRedactStrings.add(readApiToken);
			Logger apiLogger = LogUtils.getLogger(this.getClass().getCanonicalName(), apiRedactStrings);
			
			readApi      = new ReadApi(apiLogger);
			videoFields  = VideoFieldEnum.CreateEmptyEnumSet();
			customFields = CollectionUtils.CreateEmptyStringSet();
			
			if((videoFieldsProp != null) && (videoFieldsProp.getValue() != null)){
				for(String videoField : videoFieldsProp.getValue().split(",")){
					videoFields.add(VideoFieldEnum.valueOf(videoField));
				}
			}
			
			if((customFieldsProp != null) && (customFieldsProp.getValue() != null)){
				for(String customField : customFieldsProp.getValue().split(",")){
					customFields.add(customField);
				}
			}
			
			videoStateFilter = new HashSet<VideoStateFilterEnum>();
			if((videoStateProp != null) && (videoStateProp.getValue() != null)){
				for(String state : videoStateProp.getValue().split(",")){
					VideoStateFilterEnum filter = VideoStateFilterEnum.valueOf(state);
					videoStateFilter.add(filter);
				}
			}
			else{
				videoStateFilter.add(VideoStateFilterEnum.PLAYABLE);
			}
			
			fromDate = 0l;
			if((fromDateProp != null) && (fromDateProp.getValue() != null)){
				Date date = null;
				try {
					date = dateFormatter.parse(fromDateProp.getValue());
				}
				catch (ParseException pe) {
					die("VideoCloudFindModifiedInputAdapter couldn't parse date '" + fromDateProp.getValue() + "' with simple date format '" + dateFormat + "'.  Exception caught: '" + pe + "'.");
				}
				
				// Have milliseconds, need minutes
				Long oneMinute = 1000l * 60l;
				fromDate = date.getTime() / oneMinute;
			}
			
			pageSize   = 100;
			pageNumber = 0;
			
			sortBy        = SortByTypeEnum.MODIFIED_DATE;
			sortOrderType = SortOrderTypeEnum.DESC;
			
			Videos videos = getPage();
			while((videos != null) && (videos.size() > 0)){
				
				for(Video video : videos){
					Record          record = new Record();
					Property<Video> prop   = new Property<Video>("video", video);
					record.addProperty(prop);
					
					handleRecord(record);
				}
				
				pageNumber++;
				videos = getPage();
			}
		}
		
		super.run();
	}
	
	private Videos getPage(){
		Videos videos = null;
		try {
			videos = readApi.FindModifiedVideos(readApiToken, fromDate, videoStateFilter, pageSize, pageNumber, sortBy, sortOrderType, videoFields, customFields);
		}
		catch (BrightcoveException be) {
			die("Couldn't obtain page of videos.  Exception caught: '" + be + "'.");
		}
		
		return videos;
	}
}
