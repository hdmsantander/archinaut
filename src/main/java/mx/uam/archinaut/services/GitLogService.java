package mx.uam.archinaut.services;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import mx.uam.archinaut.data.loader.LogLoader;
import mx.uam.archinaut.data.loader.MetricsLoader;
import mx.uam.archinaut.data.nameprocessing.NameProcessor;
import mx.uam.archinaut.model.DependencyMetric;
import mx.uam.archinaut.model.DesignStructureMatrix;
import mx.uam.archinaut.model.ElementMetric;
import mx.uam.archinaut.model.MatrixDependencyGroup;
import mx.uam.archinaut.model.MatrixElement;

@Slf4j
@Service
public class GitLogService {

	private LogLoader loader;

	@Autowired
	private NameProcessor nameProcessor;

	public GitLogService(LogLoader analizador) {
		this.loader = analizador;
	}

}
