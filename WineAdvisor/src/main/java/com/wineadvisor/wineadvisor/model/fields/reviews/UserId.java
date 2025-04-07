package com.wineadvisor.wineadvisor.model.fields.reviews;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserId {
    @Schema(description = "Username", example = "user123")
    private String username;

    @Schema(description = "Thumbnail picture", example = "https://randomlink.extension/path/subpath/img_thumb.jpg")
    private String thumbnail;
}
