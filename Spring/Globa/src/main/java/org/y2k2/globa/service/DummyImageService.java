package org.y2k2.globa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.repository.DummyImageRepository;

@Service
@RequiredArgsConstructor
public class DummyImageService {
    public final DummyImageRepository dummyImageRepository;
}
