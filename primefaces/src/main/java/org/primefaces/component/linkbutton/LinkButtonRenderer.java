/*
 * The MIT License
 *
 * Copyright (c) 2009-2024 PrimeTek Informatics
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.primefaces.component.linkbutton;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.primefaces.renderkit.OutcomeTargetRenderer;
import org.primefaces.util.HTML;
import org.primefaces.util.LangUtils;
import org.primefaces.util.WidgetBuilder;

public class LinkButtonRenderer extends OutcomeTargetRenderer {

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        LinkButton linkButton = (LinkButton) component;

        encodeMarkup(context, linkButton);
        encodeScript(context, linkButton);
    }

    protected void encodeMarkup(FacesContext context, LinkButton linkButton) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        boolean disabled = linkButton.isDisabled();
        boolean hasIcon = LangUtils.isNotBlank(linkButton.getIcon());
        boolean hasValue = LangUtils.isNotBlank((String) linkButton.getValue()) || linkButton.hasDisplayedChildren();
        boolean isTextAndIcon = hasValue && hasIcon;

        String style = linkButton.getStyle();
        String title = linkButton.getTitle();
        String styleClass = getStyleClassBuilder(context)
                    .add("ui-linkbutton")
                    .add(linkButton.getStyleClass())
                    .add(hasValue && !hasIcon, HTML.BUTTON_TEXT_ONLY_BUTTON_CLASS)
                    .add(!hasValue && hasIcon, HTML.BUTTON_ICON_ONLY_BUTTON_CLASS)
                    .add(isTextAndIcon && "left".equals(linkButton.getIconPos()), HTML.BUTTON_TEXT_ICON_LEFT_BUTTON_CLASS)
                    .add(isTextAndIcon && "right".equals(linkButton.getIconPos()), HTML.BUTTON_TEXT_ICON_RIGHT_BUTTON_CLASS)
                    .add(disabled, "ui-state-disabled")
                    .build();

        writer.startElement("span", linkButton);
        writer.writeAttribute("id", linkButton.getClientId(context), "id");
        writer.writeAttribute("class", styleClass, "styleClass");
        if (style != null) {
            writer.writeAttribute("style", style, "style");
        }
        if (title != null) {
            writer.writeAttribute("title", title, "title");
        }
        renderPassThruAttributes(context, linkButton, HTML.OUTPUT_EVENTS_WITHOUT_CLICK);

        String targetURL = getTargetURL(context, linkButton);
        if (targetURL == null) {
            targetURL = "#";
        }

        writer.startElement("a", null);
        writer.writeAttribute("href", targetURL, null);
        renderPassThruAttributes(context, linkButton, HTML.LINK_ATTRS_WITHOUT_EVENTS_AND_STYLE, HTML.TITLE);
        renderDomEvents(context, linkButton, HTML.OUTPUT_EVENTS);
        renderContent(context, linkButton);
        writer.endElement("a");

        writer.endElement("span");
    }

    protected void encodeScript(FacesContext context, LinkButton button) throws IOException {
        WidgetBuilder wb = getWidgetBuilder(context);
        wb.init("LinkButton", button);
        wb.finish();
    }

    protected void renderContent(FacesContext context, LinkButton linkButton) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        String icon = linkButton.getIcon();
        if (!isValueBlank(icon)) {
            String defaultIconClass = linkButton.getIconPos().equals("left") ? HTML.BUTTON_LEFT_ICON_CLASS : HTML.BUTTON_RIGHT_ICON_CLASS;
            String iconClass = defaultIconClass + " " + icon;

            writer.startElement("span", null);
            writer.writeAttribute("class", iconClass, null);
            writer.endElement("span");
        }

        writer.startElement("span", null);
        writer.writeAttribute("class", HTML.BUTTON_TEXT_CLASS, null);

        String value = (String) linkButton.getValue();
        if (value == null) {
            if (linkButton.hasDisplayedChildren()) {
                renderChildren(context, linkButton);
            }
            else {
                //For ScreenReader
                renderButtonValue(writer, linkButton.isEscape(), null, linkButton.getTitle(), linkButton.getAriaLabel());
            }
        }
        else {
            renderButtonValue(writer, linkButton.isEscape(), value, linkButton.getTitle(), linkButton.getAriaLabel());
        }

        writer.endElement("span");
    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        //Do nothing
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }
}
