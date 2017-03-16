package com.google.android.libraries.mediaframework.exoplayerextensions;

import java.util.List;
import java.util.Map;

/**
 * Represents net request data.
 * Created by zanol on 3/16/17
 */

public final class RequestData {

	public final String url;
	public final String method;
	public final String permission;
	public final int responseCode;
	public final String responseMessage;
	public final Map<String, List<String>> headers;
	public final String content;
	public final String errorMessage;

	public RequestData(String url, String method, String permission, int responseCode, String responseMessage,
	                   Map<String, List<String>> headers, String content, String errorMessage) {
		this.url = url;
		this.method = method;
		this.permission = permission;
		this.responseCode = responseCode;
		this.responseMessage = responseMessage;
		this.headers = headers;
		this.content = content;
		this.errorMessage = errorMessage;
	}

	@Override
	public String toString() {
		return String.format("URL: %s\nRequest method: %s\nPermission: %s\nResponse code: %s\nResponse msg: %s\nHeaders: %s\nContent: %s\nError: %s",
				url, method, permission, responseCode, responseMessage, headers, content, errorMessage);
	}
}
