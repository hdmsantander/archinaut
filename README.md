# Archinaut

An open source tool to integrate metric results from other source code analysis tools. It generates a [CSV](https://en.wikipedia.org/wiki/Comma-separated_values) file named _archinaut.csv_ containing all the combined metrics provided.
It can also generate a XML report in the [JUnit](https://www.ibm.com/docs/en/z-open-unit-test/2.0.x?topic=SSZHNR_2.0.0/com.ibm.rsar.analysis.codereview.cobol.doc/topics/cac_useresults_junit.html) standard, given thresholds over the metrics analyzed by the tool.

Usage: `java -jar archinaut.jar --configuration archinaut.yml`

## Configuration file

Path to the configuration file that holds the Archinaut settings in a YAML format. The configuration file is divided by sections, each section being a source of metrics (metric report) that can be integrated by Archinaut. The current **formats** recognized by Archinaut are:

- [CSV](https://en.wikipedia.org/wiki/Comma-separated_values)
- [DEPENDS](https://github.com/multilang-depends/depends)

The **file** declared in each section must be an existing file, reachable by Archinaut at runtime.

The **renaming** section is used to standarize names of the objects inside the metric reports, prefixes and suffixes are removed and then substitutions of characters are performed in the order defined.

The **metrics** section is used to declare the numeric (integer) metrics that are to be loaded from the metric reports. The one marked with the boolean _filename_ serves as the identifier for the filename in the report, there can only be one _filename_ flag specified. The metrics can be renamed if a **rename** is specified.

The metric report provided by [depends](https://github.com/multilang-depends/depends) is non-optional and its generated with the following [depends](https://github.com/multilang-depends/depends) options: `java -jar $DEPENDS_JAR -s -p dot -d $HOME java ./src depends`

An example of the _archinaut.yml_ file can be seen here:

_archinaut.yml_

```YAML
---
file: 'scc.csv'
format: 'CSV'
renaming:
  pathSeparator: '/'
  prefix: 'src/main/java/'
  suffix: ''
  substitutions:
    - order: 1
      substitute: '.'
      with: '_'
    - order: 2
      substitute: '/'
      with: '_'
metrics:
  - name: 'Location'
    filename: true
  - name: 'Lines'
    rename: 'SCC_LOC'
  - name: 'Code'
    rename: 'SCC_CLOC'
  - name: 'Complexity'
    rename: 'SCC_COMPLEXITY'
---
file: 'frequencies.csv'
format: 'CSV'
renaming:
  pathSeparator: '/'
  prefix: 'src/main/java/'
  suffix: ''
  substitutions:
    - order: 1
      substitute: '.'
      with: '_'
    - order: 2
      substitute: '/'
      with: '_'
metrics:
  - name: 'entity'
    filename: true
  - name: 'n-revs'
    rename: 'ARCH_REVISIONS'
  - name: 'bugs'
    rename: 'BUG_COMMITS'
  - name: 'added'
    rename: 'LINES_ADDED'
  - name: 'removed'
    rename: 'LINES_REMOVED'
---
file: 'coupling.csv'
format: 'CSV'
renaming:
  pathSeparator: '/'
  prefix: 'src/main/java/'
  suffix: ''
  substitutions:
    - order: 1
      substitute: '.'
      with: '_'
    - order: 2
      substitute: '/'
      with: '_'
metrics:
  - name: 'entity'
    filename: true
  - name: 'cochanges'
    rename: 'COCHANGES'
---
file: 'depends.json'
format: 'DEPENDS'
renaming:
  pathSeparator: '.'
  prefix: 'main.java.'
  suffix: ''
  substitutions:
    - order: 1
      substitute: '.'
      with: '_'
metrics:
  - name: 'Call'
  - name: 'Import'
  - name: 'Return'
  - name: 'Use'
  - name: 'Parameter'
  - name: 'Contain'
  - name: 'Implement'
  - name: 'Create'
  - name: 'Extend'

```

## Threshold inputs

These inputs are _optional_ and serve to generate a JUnit format XML report with the threshold violations. These inputs are provided in the from of environment variables.

Given any **metrics** declared in the configuration file, an input can be declared in the action specification, that will work as a threshold to generate a JUnit style XML report with the violations of said thresholds. For example, in the **archinaut.yml** file we specified the metrics _SCC_LOC_, _SCC_CLOC_ and _SCC_COMPLEXITY_, so we can declare the following inputs:

- INPUT_SCC_LOC=150
- INPUT_SCC_CLOC=100
- INPUT_SCC_COMPLEXITY=15

These inputs will be parsed and used at runtime to generate a JUnit style XML report named _archinaut.xml_ with the violations detected.
