/*
 * Copyright 2018 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thoughtworks.go.config.parser;

import com.thoughtworks.go.config.AttributeAwareConfigTag;
import com.thoughtworks.go.config.ConfigCache;
import com.thoughtworks.go.config.ConfigTag;
import com.thoughtworks.go.config.preprocessor.ClassAttributeCache;
import com.thoughtworks.go.config.registry.ConfigElementImplementationRegistry;
import com.thoughtworks.go.security.GoCipher;
import org.jdom2.Element;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class GoConfigClassLoaderTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    @Mock
    private ConfigCache configCache;
    @Mock
    private GoCipher goCipher;
    @Mock
    private ConfigElementImplementationRegistry registry;
    @Mock
    private ConfigReferenceElements referenceElements;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldErrorOutIfElementDoesNotHaveConfigTagAnnotation() {
        final Element element = new Element("cruise");
        final GoConfigClassLoader<ConfigWithoutAnnotation> loader = GoConfigClassLoader.classParser(element, ConfigWithoutAnnotation.class, configCache, goCipher, registry, referenceElements);

        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Unable to parse element <cruise> for class ConfigWithoutAnnotation");
        loader.parse();
    }

    @Test
    public void shouldContinueParsingWhenElementHasConfigTagAnnotation() {
        final Element element = new Element("example");
        when(configCache.getFieldCache()).thenReturn(new ClassAttributeCache.FieldCache());

        final GoConfigClassLoader<ConfigWithConfigTagAnnotation> loader = GoConfigClassLoader.classParser(element, ConfigWithConfigTagAnnotation.class, configCache, goCipher, registry, referenceElements);

        final ConfigWithConfigTagAnnotation configWithConfigTagAnnotation = loader.parse();

        assertNotNull(configWithConfigTagAnnotation);
    }

    @Test
    public void shouldContinueParsingWhenElementHasValidAttributeAwareConfigTagAnnotation() {
        final Element element = new Element("example");
        element.setAttribute("type", "example-type");
        when(configCache.getFieldCache()).thenReturn(new ClassAttributeCache.FieldCache());

        final GoConfigClassLoader<ConfigWithAttributeAwareConfigTagAnnotation> loader = GoConfigClassLoader.classParser(element, ConfigWithAttributeAwareConfigTagAnnotation.class, configCache, goCipher, registry, referenceElements);

        final ConfigWithAttributeAwareConfigTagAnnotation configWithConfigTagAnnotation = loader.parse();

        assertNotNull(configWithConfigTagAnnotation);
    }

    @Test
    public void shouldErrorOutWhenElementHasAttributeAwareConfigTagAnnotationButAttributeValueIsNotMatching() {
        final Element element = new Element("example");
        element.setAttribute("type", "foo-bar");
        when(configCache.getFieldCache()).thenReturn(new ClassAttributeCache.FieldCache());

        final GoConfigClassLoader<ConfigWithAttributeAwareConfigTagAnnotation> loader = GoConfigClassLoader.classParser(element, ConfigWithAttributeAwareConfigTagAnnotation.class, configCache, goCipher, registry, referenceElements);


        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Unable to determine type to generate. Type: com.thoughtworks.go.config.parser.ConfigWithAttributeAwareConfigTagAnnotation Element: \n" +
                "\t<example type=\"foo-bar\" />");

        loader.parse();
    }

    @Test
    public void shouldErrorOutWhenElementHasAttributeAwareConfigTagAnnotationButAttributeIsNotPresent() {
        final Element element = new Element("example");
        when(configCache.getFieldCache()).thenReturn(new ClassAttributeCache.FieldCache());

        final GoConfigClassLoader<ConfigWithAttributeAwareConfigTagAnnotation> loader = GoConfigClassLoader.classParser(element, ConfigWithAttributeAwareConfigTagAnnotation.class, configCache, goCipher, registry, referenceElements);


        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Unable to determine type to generate. Type: com.thoughtworks.go.config.parser.ConfigWithAttributeAwareConfigTagAnnotation Element: \n" +
                "\t<example />");

        loader.parse();
    }
}

class ConfigWithoutAnnotation {

}

@ConfigTag("example")
class ConfigWithConfigTagAnnotation {
}

@AttributeAwareConfigTag(value = "example", attribute = "type", attributeValue = "example-type")
class ConfigWithAttributeAwareConfigTagAnnotation {
}