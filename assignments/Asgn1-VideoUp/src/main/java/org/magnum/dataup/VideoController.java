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
import org.magnum.dataup.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

@Controller
public class VideoController {
    private final VideoService service;

    @Autowired
    public VideoController (VideoService service) {
        this.service = service;
    }

    @RequestMapping(value = "/video")
    @ResponseBody
    public Collection<Video> getVideos() {
        return service.getVideos();
    }

    @RequestMapping(value = "/video", method = RequestMethod.POST)
    @ResponseBody
    public Video createVideo(@RequestBody Video video) {
        return service.createVideo(video);
    }

    @RequestMapping(value = "/video/{id}/data")
    public void getVideoData (@PathVariable("id") long videoId, HttpServletResponse response) throws IOException {
        service.streamVideo(videoId, response);
    }

    @RequestMapping(value = "/video/{id}/data", method = RequestMethod.POST)
    @ResponseBody
    public VideoStatus postVideoData(@PathVariable("id") long videoId,
                                     @RequestParam("data") MultipartFile videoData) throws IOException {
        return service.setVideoData(videoId, videoData);
    }
}
