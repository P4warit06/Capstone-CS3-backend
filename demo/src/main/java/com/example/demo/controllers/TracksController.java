package com.example.demo.controllers;

import com.example.demo.dtos.TrackDetailDto;
import com.example.demo.dtos.TrackDto;
import com.example.demo.services.tracks.TracksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
    @RequestMapping("/v1/tracks")
public class TracksController {

    @Autowired
    private TracksService tracksService;

    @GetMapping
    public List<TrackDto> getAllTracks(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Integer userId = extractUserId(authHeader);
        return tracksService.getAllTracks(userId);
    }

    @GetMapping("/{id}")
    public TrackDetailDto getTrackById(
            @PathVariable Integer id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        Integer userId = extractUserId(authHeader);
        return tracksService.getTrackById(id, userId);
    }

    private Integer extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        try {
            
            String token = authHeader.substring(7);
            return Integer.parseInt(token);
        } catch (Exception e) {
            return null;
        }
    }
}
