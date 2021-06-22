package mx.uam.archinaut.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import mx.uam.archinaut.model.DesignStructureMatrix;
import mx.uam.archinaut.model.MatrixElement;

@SpringBootTest
class DesignStructureMatrixServiceTest extends AbstractServiceTest {
	
	@Autowired
	private DesignStructureMatrixService designStructureMatrixService;
	
	@Test
	void loadMatrixFromJSONTest() {
		
		String[] filenames = {"main.java.com.uam.spaceinvaders.App_java",
			    "main.java.com.uam.spaceinvaders.audio.Audio_java",
			    "main.java.com.uam.spaceinvaders.entities.Sandbag_java",
			    "main.java.com.uam.spaceinvaders.entities.Spaceship_java",
			    "main.java.com.uam.spaceinvaders.entities.aliens.Alien_java",
			    "main.java.com.uam.spaceinvaders.entities.aliens.GreenAlien_java",
			    "main.java.com.uam.spaceinvaders.entities.aliens.RedAlien_java",
			    "main.java.com.uam.spaceinvaders.entities.aliens.WhiteAlien_java",
			    "main.java.com.uam.spaceinvaders.entities.aliens.YellowAlien_java",
			    "main.java.com.uam.spaceinvaders.entities.interfaces.MovableComponent_java",
			    "main.java.com.uam.spaceinvaders.entities.projectiles.AlienProjectile_java",
			    "main.java.com.uam.spaceinvaders.entities.projectiles.Projectile_java",
			    "main.java.com.uam.spaceinvaders.entities.projectiles.SpaceshipProjectile_java",
			    "main.java.com.uam.spaceinvaders.levels.LevelOne_java",
			    "main.java.com.uam.spaceinvaders.levels.Level_java",
			    "main.java.com.uam.spaceinvaders.presentation.GameFrame_java",
			    "main.java.com.uam.spaceinvaders.presentation.GamePanel_java"};
		
		DesignStructureMatrix matrix = designStructureMatrixService.loadMatrixFromJSON(dependsConfigurationEntry);
		assertNotNull(matrix);
		
		assertEquals(17, matrix.getElementsCount());
		
		int i = 0;
		for (MatrixElement m : matrix.getElements()) {
			assertEquals(true, filenames[i].contains(m.getFullName()));
			i++;
		}
		
	}
	
}
