package com.sharmagagan413.photoapp.api.albums.service;


import java.util.List;

import com.sharmagagan413.photoapp.api.albums.data.AlbumEntity;

public interface AlbumsService {
    List<AlbumEntity> getAlbums(String userId);
}
