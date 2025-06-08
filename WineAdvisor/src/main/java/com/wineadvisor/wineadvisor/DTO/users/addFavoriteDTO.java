package com.wineadvisor.wineadvisor.DTO.users;

import com.wineadvisor.wineadvisor.model.users.fields.WineFavorite;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class addFavoriteDTO {
    @NotBlank(message = "wineId cannot be blank.")
    @Positive(message = "wineId must be a positive number.")
    @Schema(description = "The ID of the wine to be added to favorites.", example = "1")
    private Long wineId;

    @NotBlank(message = "Name cannot be blank.")
    @Schema(description = "The name of the wine.", example = "Nome per un vino")
    private String name;

    @Schema(description = "Image info of the new vintage", example = "https://example.com/image.jpg")
    private String image;



    ///////////// METODI PUBBLICI /////////////
    // Ritorna un oggetto WineFavorite a partire da un oggetto addFavoriteDTO
    public WineFavorite toWineFavorite() {
        return new WineFavorite(
            this.wineId,
            this.name,
            this.image
        );
    }
}
