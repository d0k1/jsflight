package com.focusit.jsflight.recorder;

public interface RecordingProcessor {
	void processDownloadRequest(String data);
	void processStoreEvent(String data);
}
