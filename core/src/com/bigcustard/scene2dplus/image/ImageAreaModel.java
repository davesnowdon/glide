package com.bigcustard.scene2dplus.image;

import java.util.List;

public interface ImageAreaModel {
    ImagePlus addImage(String imageUrl);
    List<ImagePlus> getImages();
    List<ImageValidator.Result> validateImages();
    void deleteImage(ImagePlus image);
}
