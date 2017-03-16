package com.sambatech.player.model;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.sambatech.player.R;

/**
 * Data entity that represents a request media request to the server.
 *
 * @author Leandro Zanol 11/12/15
 */
public class SambaMediaRequest {

	public String projectHash;
	public String mediaId;
	public String streamName;
	public String streamUrl;
	public @NonNull String[] backupUrls = new String[]{};
	public Environment environment = Environment.PROD;

	/**
	 * Represents a VOD media request.
	 *
	 * @param projectHash Hash code of the project
	 * @param mediaId Hash code of the media
	 */
	public SambaMediaRequest(String projectHash, String mediaId) {
		this(projectHash, mediaId, null);
	}

	/**
	 * Represents a live stream request (by stream name).
	 *
	 * @param projectHash Hash code of the project
	 * @param mediaId Hash code of the media
	 * @param streamName Name of the stream (live only)
	 */
	public SambaMediaRequest(String projectHash, String mediaId, String streamName) {
		this(projectHash, mediaId, streamName, null);
	}

	/**
	 * Represents a direct live stream request (by URL).
	 *
	 * @param projectHash Hash code of the project
	 * @param mediaId Hash code of the media
	 * @param streamName Name of the stream (live only)
	 * @param streamUrl URL for stream (`streamName` will be ignored)
	 */
	public SambaMediaRequest(String projectHash, String mediaId, String streamName, String streamUrl) {
		this(projectHash, mediaId, streamName, streamUrl, null);
	}

	/**
	 * Represents a direct live stream request (by URL) with other backup URLs.
	 *
	 * @param projectHash Hash code of the project
	 * @param mediaId Hash code of the media
	 * @param streamName Name of the stream (live only)
	 * @param streamUrl URL for stream (`streamName` will be ignored)
	 * @param backupUrls URL list for fallback purposes
	 */
	public SambaMediaRequest(String projectHash, String mediaId, String streamName, String streamUrl, String[] backupUrls) {
		this.projectHash = projectHash;
		this.mediaId = mediaId;
		this.streamName = streamName;
		this.streamUrl = streamUrl;
		this.backupUrls = backupUrls != null ? backupUrls : new String[]{};
	}

	@Override
	public String toString() {
		return String.format("projectHash: %s, id: %s, streamName: %s, streamUrls: %s, backupUrls (count): %s", projectHash, mediaId, streamName, streamUrl, backupUrls.length);
	}

	public String getEndpoint(Activity activity) {
		switch (environment) {
			case LOCAL:
				return activity.getString(R.string.player_endpoint_local);
			case DEV:
				return activity.getString(R.string.player_endpoint_test);
			case STAGING:
				return activity.getString(R.string.player_endpoint_staging);
			case PROD:
			default:
				return activity.getString(R.string.player_endpoint_prod);
		}
	}

	public enum Environment {
		LOCAL,
		DEV,
		STAGING,
		PROD
	}
}
