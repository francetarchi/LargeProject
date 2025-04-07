<<<<<<<< HEAD:WineAdvisor/src/main/java/com/wineadvisor/wineadvisor/model/fields/users/Picture.java
package com.wineadvisor.wineadvisor.model.fields.users;
========
package com.wineadvisor.wineadvisor.model.fields.user;
>>>>>>>> origin/valentina:WineAdvisor/src/main/java/com/wineadvisor/wineadvisor/model/fields/user/Picture.java

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class Picture {
    @Schema(description = "Large picture", example = "https://randomlink.extension/path/subpath/img_large.jpg")
    private String large;

    @Schema(description = "Medium picture", example = "https://randomlink.extension/path/subpath/img_medium.jpg")
    private String medium;

    @Schema(description = "Thumbnail picture", example = "https://randomlink.extension/path/subpath/img_thumb.jpg")
    private String thumbnail;
}
