package org.swisspush.jsons2xsd;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.ethlo.jsons2xsd.Config;
import com.ethlo.jsons2xsd.JsonSimpleType;
import com.ethlo.jsons2xsd.Jsons2Xsd;
import com.ethlo.jsons2xsd.XsdSimpleType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.w3c.dom.Document;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Arrays;

/**
 * Converts JSON Schemas to XML Schemas.
 **/
@Mojo(name="convert", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class Jsons2XsdMojo extends AbstractMojo
{
    @Parameter(defaultValue = "${project.basedir}/src/main/json-schema")
    private File inputDirectory;

    @Parameter(defaultValue = "${project.build.directory}/classes/xsd")
    private File outputDirectory;

    @Parameter(required = true)
    private String namespace;

    @Parameter(defaultValue = "ns")
    private String namespaceAlias;

    @Parameter(defaultValue = "true")
    private boolean useGenericItemNames;

    public void execute() throws MojoExecutionException {
        outputDirectory.mkdirs();

        File[] files = inputDirectory.listFiles();
        if(files != null) {
            Arrays.stream(files).forEach(input -> {
                File output = new File(outputDirectory, input.getName().replace(".json", ".xsd"));
                try {
                    generate(input, output);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } else {
            throw new MojoExecutionException("Cannot list files from input directory: "+inputDirectory.toString());
        }
    }

    private void generate(File input, File output) throws IOException {

        String name;
        JsonNode idNode = new ObjectMapper().readTree(new FileReader(input)).get("id");
        if (idNode != null) {
            name = idNode.textValue();
        } else {
            throw new RuntimeException("Schema should have an id");
        }

        final Config.Builder cfgBuilder = new Config.Builder()
                .targetNamespace(namespace)
                .name(name)
                .nsAlias(namespaceAlias)
                .rootElement(name.substring(0,1).toLowerCase()+name.substring(1))
                .customTypeMapping(JsonSimpleType.INTEGER, "long", XsdSimpleType.LONG)
                .customTypeMapping(JsonSimpleType.INTEGER, "int64", XsdSimpleType.LONG)
                .customTypeMapping(JsonSimpleType.STRING, "uuid", XsdSimpleType.STRING)
                .customTypeMapping(JsonSimpleType.NUMBER, "double", XsdSimpleType.DECIMAL)
                .customTypeMapping(JsonSimpleType.NUMBER, "float", XsdSimpleType.DECIMAL)
                .ignoreUnknownFormats(true)
                .validateXsdSchema(false);

        if(useGenericItemNames) {
            cfgBuilder.mapArrayItemNames(x -> "item");
        }

        final Document doc = Jsons2Xsd.convert(new FileReader(input), cfgBuilder.build());
        writeXml(doc, new FileWriter(output));
    }

    private static void writeXml(Document xmlDocument, Writer writer)
    {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tf.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        try {
            transformer.transform(new DOMSource(xmlDocument), new StreamResult(writer));
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }
}
