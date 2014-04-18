/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.weld.tests.contexts.conversation.timeout;

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.tests.category.Integration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

/**
 * @author Marko Luksa
 * @author Martin Kouba
 *
 * @see WELD-1452
 * @see WELD-1657
 */
@RunWith(Arquillian.class)
@Category(Integration.class)
public class ConversationTimeoutTest {

    @ArquillianResource
    private URL url;

    @Deployment(testable = false)
    public static WebArchive getDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(TimeoutController.class, TimeoutConversationScopedBean.class, TimeoutFilter.class)
                .addAsWebResource(ConversationTimeoutTest.class.getPackage(), "form.xhtml", "form.xhtml")
                .addAsWebResource(ConversationTimeoutTest.class.getPackage(), "test.xhtml", "test.xhtml")
                .addAsWebInfResource(ConversationTimeoutTest.class.getPackage(), "web.xml", "web.xml")
                .addAsWebInfResource(ConversationTimeoutTest.class.getPackage(), "faces-config.xml", "faces-config.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testConversationTimesOut() throws Exception {

        WebClient client = new WebClient();
        client.setThrowExceptionOnFailingStatusCode(false);

        HtmlPage page = client.getPage(url + "form.jsf");

        // Begin conversation
        HtmlSubmitInput buttonBegin = page.getDocumentElement().getElementById("buttonBegin");
        page = buttonBegin.click();

        // Wait for conversation to time out
        Thread.sleep(1100);

        HtmlSubmitInput buttonPing = page.getDocumentElement().getElementById("buttonPing");
        TextPage errorPage = buttonPing.click();

        assertEquals(TimeoutFilter.NON_EXISTENT_CONVERSATION, errorPage.getContent().trim());
    }

    @Test
    public void testConversationDoesNotTimeOutOnRedirect() throws Exception {

        WebClient client = new WebClient();
        client.setThrowExceptionOnFailingStatusCode(true);

        HtmlPage page = client.getPage(url + "form.jsf");

        // Begin conversation
        HtmlSubmitInput buttonBegin = page.getDocumentElement().getElementById("buttonBegin");
        page = buttonBegin.click();
        String cid = getConversationId(page);

        // Conversation will expire in middle of request but should not timeout
        // JSF does redirect at the end of the request
        HtmlSubmitInput buttonLong = page.getDocumentElement().getElementById("buttonLong");
        page = buttonLong.click();

        assertEquals(200, page.getWebResponse().getStatusCode());
        assertEquals("TEST", page.getTitleText().trim());
        assertEquals(cid, getConversationId(page));
    }

    private String getConversationId(HtmlPage page) {
        HtmlSpan spanCid = page.getDocumentElement().getElementById("conversationId");
        return spanCid.getTextContent().trim();
    }

}
