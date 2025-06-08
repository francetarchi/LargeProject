package com.wineadvisor.wineadvisor.DTO.utils;

import com.wineadvisor.wineadvisor.model.utils.Picture;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PictureDTO {
    @Schema(description = "Large picture", example = "https://randomlink.extension/path/subpath/img_large.jpg")
    private String large;

    @Schema(description = "Medium picture", example = "https://randomlink.extension/path/subpath/img_medium.jpg")
    private String medium;

    @Schema(description = "Thumbnail picture", example = "https://randomlink.extension/path/subpath/img_thumb.jpg")
    private String thumbnail;



    ///////////// METODI PUBBLICI /////////////
    public Picture toPicture() {
        return new Picture(this.large, this.medium, this.thumbnail);
    }
}
