package com.brightcove.opensource.workflowengine.actors;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.brightcove.commons.applications.FTPUploader;
import com.brightcove.commons.catalog.objects.Video;
import com.brightcove.commons.xml.XalanUtils;
import com.brightcove.opensource.workflowengine.Actor;
import com.brightcove.opensource.workflowengine.Workflow;
import com.brightcove.opensource.workflowengine.record.Property;
import com.brightcove.opensource.workflowengine.record.Record;

/**
 * <p>
 *    Takes each Record provided by downstream providers and uses it to
 *    issue a command to re-encode that video from existing source.
 * </p>
 * 
 * <p>
 *    This is an intensive job on the Brightcove System, so a delay should
 *    be introduced between each job request.  The length of that delay should
 *    be worked out with Brightcove Consulting or Brightcove Customer Support,
 *    but should be AT LEAST as long as the average length of the videos
 *    being re-encoded
 * </p>
 * 
 * <p>
 *    The following configuration properties will be honored:<ul>
 *        <li>video-reference-id-prop - Property name on each record that
 *            stores the video reference id to be re-encoded.</li>
 *        <li>batch-username - Batch provisioning FTP username.</li>
 *        <li>batch-password - Batch provisioning FTP password.</li>
 *        <li>publisher-id - Video Cloud publisher id (account id).</li>
 *        <li>preparer - Your name and company.</li>
 *        <li>notification-email - Video Cloud will send email when re-encode for each title is underway.</li>
 *        <li>overwrite-images - If TRUE, existing images will be overwritten by transcode process.</li>
 *        <li>preserve-source-as-rendition - If TRUE, mezzanine file will appear as one of transcoded video's renditions.</li>
 *    </ul>
 * </p>
 * 
 * @author <a href="https://github.com/three4clavin">Sander Gates</a>
 *
 */
public class VideoCloudReEncodeFromExistingSourceAdapter extends Actor {
	private String   videoRefIdName  = null;
	private String   batchUsername   = null;
	private String   batchPassword   = null;
	private String   publisherId     = null;
	private String   preparer        = null;
	private String   notifyEmail     = null;
	private Boolean  overwriteImages = null;
	private Boolean  preserveSource  = null;
	
	/**
	 * <p>
	 *    Default constructor - workflow this actor is part of must be
	 *    provided
	 * </p>
	 * 
	 * @param workflow Workflow this actor will be part of
	 */
	public VideoCloudReEncodeFromExistingSourceAdapter(Workflow workflow){
		super(workflow);
	}
	
	/* (non-Javadoc)
	 * @see com.brightcove.opensource.workflowengine.Actor#run()
	 */
	public void run(){
		if(! hasStarted()){
			videoRefIdName = getFirstPropertyValue("video-reference-id-prop");
			
			batchUsername  = getFirstPropertyValue("batch-username");
			batchPassword  = getFirstPropertyValue("batch-password");
			publisherId    = getFirstPropertyValue("publisher-id");
			preparer       = getFirstPropertyValue("preparer");
			notifyEmail    = getFirstPropertyValue("notification-email");
			
			if(videoRefIdName == null){
				die("'video-reference-id-prop' must be specified to the VideoCloudReEncodeFromExistingSource actor.");
			}
			
			if((batchUsername == null) || (batchPassword == null)){
				die("'batch-username' and 'batch-password' must be specified to the VideoCloudReEncodeFromExistingSource actor.");
			}
			
			if(publisherId == null){
				die("'publisher-id' must be specified to the VideoCloudReEncodeFromExistingSource actor.");
			}
			
			if(preparer == null){
				die("'preparer' must be specified to the VideCloudReEncodeFromExistingSource actor.");
			}
			
			if("TRUE".equalsIgnoreCase(getFirstPropertyValue("overwrite-images"))){
				overwriteImages = true;
			}
			else{
				overwriteImages = false;
			}
			
			if("TRUE".equalsIgnoreCase(getFirstPropertyValue("preserve-source-as-rendition"))){
				preserveSource = true;
			}
			else{
				preserveSource = false;
			}
		}
		
		super.run();
	}
	
	/* (non-Javadoc)
	 * @see com.brightcove.opensource.workflowengine.Actor#handleRecord(com.brightcove.opensource.workflowengine.Record)
	 */
	public void handleRecord(Record record){
		Property<?> videoRefIdProp = record.getFirstProperty(videoRefIdName);
		
		Video video = new Video();
		if((videoRefIdProp != null) && (videoRefIdProp.getValue() != null) && (! "".equals(videoRefIdProp.getValue()))){
			video.setReferenceId(""+videoRefIdProp.getValue());
		}
		else{
			logError("Couldn't get ReferenceId from record '" + record + "'.");
			return;
		}
		
		log("Generating re-encode request for video '" + video + "'.");
		
		Document doc = null;
		try {
			doc = XalanUtils.createDocument("publisher-upload-manifest");
		}
		catch (ParserConfigurationException pce) {
			die("Couldn't create publisher manifest file.  Exception caught: '" + pce + "'.");
		}
		
		Element root = doc.getDocumentElement();
		root.setAttribute("publisher-id", publisherId);
		root.setAttribute("preparer", preparer);
		root.setAttribute("report-success", "TRUE");
		
		if(notifyEmail != null){
			Element notifyElem = XalanUtils.createSimpleElement(doc, "notify", null, root);
			notifyElem.setAttribute("email", notifyEmail);
		}
		
		Element reEncodeElem = XalanUtils.createSimpleElement(doc, "reencode-from-existing-source", null, root);
		reEncodeElem.setAttribute("title-refid", video.getReferenceId());
		reEncodeElem.setAttribute("encode-to", "MP4");
		reEncodeElem.setAttribute("encode-multiple", "TRUE");
		
		if(overwriteImages){
			reEncodeElem.setAttribute("overwrite-images", "TRUE");
		}
		else{
			reEncodeElem.setAttribute("overwrite-images", "FALSE");
		}
		
		if(preserveSource){
			reEncodeElem.setAttribute("preserve-source-as-rendition", "TRUE");
		}
		else{
			reEncodeElem.setAttribute("preserve-source-as-rendition", "FALSE");
		}
		
		File batchFile = new File("VideoCloudReEncodeFromExistingSource-tmp.xml");
		try {
			FileUtils.writeStringToFile(batchFile, XalanUtils.prettyPrintWithTrAX(doc), "UTF-8");
		}
		catch (IOException ioe) {
			die("Couldn't write batch XML file: '" + ioe + "'.");
		}
		catch (TransformerException te) {
			die("Couldn't write batch XML file: '" + te + "'.");
		}
		catch (ParserConfigurationException pce) {
			die("Couldn't write batch XML file: '" + pce + "'.");
		}
		
		Long        ftpTimeout  = 1000l * 60l * 60l; // 1 hour
		FTPUploader ftpUploader = new FTPUploader("upload.brightcove.com", 21, batchUsername, batchPassword, false, false, false, ftpTimeout, true);
		ftpUploader.calculateUploadMappings(batchFile.getParent(), batchFile.getName(), null, "/");
		
		// Bug somewhere in the timeout logic setting from the constructor...
		ftpUploader.setTimeoutMilliseconds(ftpTimeout);
		
		try {
			ftpUploader.doUpload();
		}
		catch (Exception e) {
			die("Couldn't upload '" + batchFile.getAbsolutePath() + "'.  Exception caught: '" + e + "'.");
		}
		
		batchFile.delete();
	}
}
