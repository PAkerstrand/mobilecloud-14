package org.magnum.dataup.service;

import org.magnum.dataup.VideoFileManager;
import org.magnum.dataup.model.Video;
import org.magnum.dataup.dao.VideoRepository;
import org.magnum.dataup.model.VideoStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

@Service
public class VideoService {
    private final VideoRepository repository;

    @Autowired
    public VideoService (VideoRepository repository) {
        this.repository = repository;
    }

    public Collection<Video> getVideos() {
        return repository.findAll();
    }

    public Video createVideo(Video video) {
        final long videoId = repository.create(video);
        video.setDataUrl(getDataUrl(videoId));
        return video;
    }


    public void streamVideo(final Long videoId, final HttpServletResponse response) throws IOException {
        final Video video = repository.findOne(videoId);
        if (video == null) {
            throw new ResourceNotFoundException("Unknown video");
        }

        final VideoFileManager manager = VideoFileManager.get();
        if (!manager.hasVideoData(video)) {
            throw new ResourceNotFoundException("Unable to stream video, no video data has been uploaded.");
        }

        response.setContentType(video.getContentType());
        manager.copyVideoData(video, response.getOutputStream());
    }

    public VideoStatus setVideoData(long videoId, MultipartFile videoData) throws IOException {
        final Video video = repository.findOne(videoId);
        if (video == null) {
            throw new ResourceNotFoundException("Unknown video");
        }

        VideoFileManager manager = VideoFileManager.get();
        manager.saveVideoData(video, videoData.getInputStream());
        return new VideoStatus(VideoStatus.VideoState.READY);
    }

    private String getDataUrl(long videoId){
        return String.format("%s/video/%d/data", getUrlBaseForLocalServer(), videoId);
    }

    private String getUrlBaseForLocalServer() {
        final HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return "http://"+request.getServerName()
                + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
    }

}
