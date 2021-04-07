/*
MIT License

Copyright (c) 2018-2019 Humberto Cervantes

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package mx.uam.archinaut.services;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import mx.uam.archinaut.model.DependencyConstraintViolation;
import mx.uam.archinaut.data.loader.ConstraintsLoader;
import mx.uam.archinaut.model.DependencyConstraint;
import mx.uam.archinaut.model.DesignStructureMatrix;
import mx.uam.archinaut.model.DesignStructureMatrixModel;
import mx.uam.archinaut.model.ElementConstraint;
import mx.uam.archinaut.model.ElementMetric;
import mx.uam.archinaut.model.MatrixDependencyGroup;
import mx.uam.archinaut.model.MatrixElement;
import mx.uam.archinaut.model.dto.DependencyConstraintDTO;
import mx.uam.archinaut.model.dto.ElementConstraintDTO;
import mx.uam.archinaut.model.dto.MatrixConstraintsDTO;

/**
 * 
 * @author humbertocervantes
 *
 */

@Service
public class ConstraintsService {
	
	@Autowired
	private ConstraintsLoader constraintsLoader;
	
	private static final Logger logger = LoggerFactory.getLogger(ConstraintsService.class);
		
	/**
	 * Add a new constraint to the matrix
	 * 
	 * @param matrix
	 * @param source
	 * @param destination
	 */
	public boolean addDependencyConstraint(DesignStructureMatrix matrix, MatrixDependencyGroup dependencyGroup) {
		DependencyConstraint constraint = new DependencyConstraint(dependencyGroup.getSource(), dependencyGroup.getDestination());
		return matrix.addDependencyConstraint(constraint);
	}

	/**
	 * Remove constraint from the matrix
	 * 
	 * @param matrix
	 * @param source
	 * @param destination
	 */
	public boolean removeDependencyConstraint(DesignStructureMatrix matrix, MatrixDependencyGroup dependencyGroup) {
		DependencyConstraint constraint = matrix.findDependencyConstraint(dependencyGroup.getSource(), dependencyGroup.getDestination());
		if(constraint != null) {
			return matrix.removeDependencyConstraint(constraint);
		}
		return false;
	}
	
	/**
	 * Add an element constraint to the matrix
	 * 
	 * @param matrix
	 * @param element
	 * @param metric
	 * @param threshold
	 * @return
	 */
	public ElementConstraint addElementConstraint(DesignStructureMatrix matrix, MatrixElement element, ElementMetric metric, int threshold) {

		for(ElementConstraint constraint:element.getConstraints()) {
			if(constraint.getMetric().equals(metric)) {
				// Tried to add a constraint for a metric that already exists
				return null;
			}
		}
		
		ElementConstraint constraint = new ElementConstraint(element, metric, threshold);
		element.addConstraint(constraint);
		matrix.addElementConstraint(constraint);
		return constraint;
	}
	
	public boolean removeElementConstraint(DesignStructureMatrix matrix, ElementConstraint constraint) {
		matrix.removeElementConstraint(constraint);
		constraint.getElement().removeConstraint(constraint);
		return true;
	}

	/**
	 * Check for constraints violations
	 * 
	 * @param matrix
	 */
	public List <DependencyConstraintViolation> checkDependencyConstraintsViolations(DesignStructureMatrix matrix) {
		
		ArrayList <DependencyConstraintViolation> violations = new ArrayList <> ();
		
		DesignStructureMatrixModel model = DesignStructureMatrixModel.getModelFromElementsInMatrix(matrix);
		
		for(int column = 0; column < model.getElementsCount(); column++)
			for(int row = 0; row < model.getElementsCount(); row++) {
				DependencyConstraint constraint = model.getDependenciesConstraint(row, column);
				if(constraint != null) {
					MatrixDependencyGroup group = model.getDependencyGroup(row, column);
					if(group.getTotalDependencies()>0) {
						DependencyConstraintViolation violation = new DependencyConstraintViolation(group, constraint);
						violations.add(violation);
					}
				}
			}

		return violations;
	}
	
	public List <ElementConstraint> checkElementConstraintViolations(DesignStructureMatrix matrix) {
		
		List <ElementConstraint> violatingConstraints = new ArrayList <>();
		
		for(ElementConstraint constraint:matrix.getElementConstraints()) {
			MatrixElement element = constraint.getElement();
			if(element.getMetricValue(constraint.getMetric())>constraint.getThreshold()) {
				// There is a violation
				violatingConstraints.add(constraint);
				logger.info("Constraint violation "+constraint+ "value: "+constraint.getMetric());
			}
			
		}
		
		return violatingConstraints;
	}
		
	/**
	 * Load constraints from JSON file
	 * 
	 * @param fileName
	 * @param matrix
	 */
	public boolean loadConstraintsFromJSON(String filename, DesignStructureMatrix matrix) {
		
		MatrixConstraintsDTO matrixDTO = constraintsLoader.loadFromJSON(filename);
		if(matrixDTO == null) {
			return false;
		}
		
		for(DependencyConstraintDTO dto:matrixDTO.getDependencyConstraints()) {
			MatrixElement source = matrix.getElementByFullname(dto.getSourceElement());
			MatrixElement destination = matrix.getElementByFullname(dto.getDestinationElement());
			if(source != null && destination != null) {
				DependencyConstraint constraint = new DependencyConstraint(source,destination);
				matrix.addDependencyConstraint(constraint);
			} else {
				logger.warn("ConstraintService.loadConstratints(): cannot create constraint for non existent element source: "+dto.getSourceElement()+" destination: "+dto.getDestinationElement());

			}
		}

		for(ElementConstraintDTO dto:matrixDTO.getElementConstraints()) {
			MatrixElement element = matrix.getElementByFullname(dto.getElementName());
			ElementMetric metric = ElementMetric.getMetricByText(dto.getMetric());
			int threshold = dto.getThreshold();
			if(element != null && metric != null) {
				ElementConstraint constraint = new ElementConstraint(element,metric,threshold);
				element.addConstraint(constraint);
				matrix.addElementConstraint(constraint);
			} else {
				logger.warn("ConstraintService.loadConstratints(): cannot create constraint for non existent element: "+dto.getElementName()+" or incorrect metric:"+dto.getMetric());

			}
		}

		return true;
		
		/*
		List <DependencyConstraint> constraints = constraintsLoader.loadFromJSON(filename, matrix);
		
		if(!constraints.isEmpty()) {
			logger.info("ConstraintService.loadConstratints():loaded "+constraints.size()+" constraints");
			for(DependencyConstraint constraint:constraints) {
				matrix.addDependencyConstraint(constraint);
			}
			return true;
		}
		return false;
		*/
	}

	/**
	 * Load constraints from JSON file
	 * 
	 * @param fileName
	 * @param matrix
	 */
	public boolean saveConstraintsToJSON(String filename, DesignStructureMatrix matrix) {
		if(!filename.endsWith(".json")) {
			filename += ".json";
		}
		
		MatrixConstraintsDTO matrixConstraintsDTO = new MatrixConstraintsDTO(matrix.getName());
		
		for(DependencyConstraint constraint:matrix.getDependencyConstraints()) {
			DependencyConstraintDTO dto = new DependencyConstraintDTO(constraint.getSource().getFullName(),constraint.getDestination().getFullName());
			matrixConstraintsDTO.addDependencyConstraint(dto);
		}
		
		for(ElementConstraint constraint:matrix.getElementConstraints()) {
			ElementConstraintDTO dto = new ElementConstraintDTO(constraint.getElement().getFullName(),constraint.getMetric().getText(),constraint.getThreshold());
			matrixConstraintsDTO.addElementConstraint(dto);
		}
		logger.info("ConstraintService.saveConstratints():"+filename);
		
		return constraintsLoader.saveToJSON(filename, matrixConstraintsDTO);
		
	}

}
