package org.magnum.dataup.model;

import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class VideoRepository {
    private final AtomicLong nextVideoId = new AtomicLong(1L);
    private final Map<Long, Video> videos = new HashMap<>();

    public Collection<Video> findAll() {
        return videos.values();
    }

    public Video findOne(final Long videoId) {
        return videos.get(videoId);
    }

    public long create(Video video) {
        if (video.getId() != 0) {
            throw new DuplicateResourceException("Video is already created");
        }
        long videoId = nextVideoId.getAndIncrement();
        video.setId(videoId);
        videos.put(video.getId(), video);
        return videoId;
    }
}
