/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.magnum.dataup;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Controller
public class VideoController {
    private final Map<Long, Video> videos = new HashMap<>();
    private final AtomicLong nextVideoId = new AtomicLong(1L);

    @RequestMapping(value = "/video")
    @ResponseBody
    public Collection<Video> getVideos() {
        return videos.values();
    }

    @RequestMapping(value = "/video", method = RequestMethod.POST)
    @ResponseBody
    public Video createVideo(@RequestBody Video video) {
        long videoId = nextVideoId.getAndIncrement();
        video.setId(videoId);
        video.setDataUrl(getDataUrl(videoId));
        videos.put(videoId, video);
        return video;
    }

    @RequestMapping(value = "/video/{id}/data")
    public void getVideoData (@PathVariable("id") long videoId, HttpServletResponse response) {
        Video video = videos.get(videoId);
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

    @RequestMapping(value = "/video/{id}/data", method = RequestMethod.POST)
    @ResponseBody
    public VideoStatus postVideoData (@PathVariable("id") long videoId,
                                      @RequestParam("data") MultipartFile videoData,
                                      HttpServletResponse response) {
        Video video = videos.get(videoId);
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

    private String getDataUrl(long videoId){
        return String.format("%s/video/%d/data", getUrlBaseForLocalServer(), videoId);
    }

    private String getUrlBaseForLocalServer() {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return "http://"+request.getServerName()
                + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
    }
}
