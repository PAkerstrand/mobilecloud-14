package org.magnum.dataup.service;

import org.magnum.dataup.VideoFileManager;
import org.magnum.dataup.model.Video;
import org.magnum.dataup.dao.VideoRepository;
import org.magnum.dataup.model.VideoStatus;
import org.springframework.beans.factory.annotation.Autowired;
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


    public void streamVideo(final Long videoId, final HttpServletResponse response) {
        Video video = repository.findOne(videoId);
        try {
            VideoFileManager manager = VideoFileManager.get();
            if (video != null && manager.hasVideoData(video)) {
                response.setContentType(video.getContentType());
                manager.copyVideoData(video, response.getOutputStream());
            } else {
                response.setStatus(404);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error while fetching video data", e);
        }
    }

    private String getDataUrl(long videoId){
        return String.format("%s/video/%d/data", getUrlBaseForLocalServer(), videoId);
    }

    private String getUrlBaseForLocalServer() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return "http://"+request.getServerName()
                + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
    }

    public VideoStatus setVideoData(long videoId, MultipartFile videoData, HttpServletResponse response) {
        Video video = repository.findOne(videoId);
        if (video == null) {
            response.setStatus(404);
            return null;
        }
        try {
            VideoFileManager manager = VideoFileManager.get();
            manager.saveVideoData(video, videoData.getInputStream());
            return new VideoStatus(VideoStatus.VideoState.READY);
        } catch (IOException e) {
            throw new RuntimeException("Error while saving video data", e);
        }
    }
}
