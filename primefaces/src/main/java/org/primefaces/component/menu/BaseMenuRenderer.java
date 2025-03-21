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
package org.primefaces.component.menu;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.primefaces.component.menubutton.MenuButton;
import org.primefaces.expression.SearchExpressionUtils;
import org.primefaces.model.menu.MenuElement;
import org.primefaces.model.menu.MenuItem;
import org.primefaces.model.menu.MenuModel;
import org.primefaces.renderkit.MenuItemAwareRenderer;
import org.primefaces.util.ComponentUtils;
import org.primefaces.util.FacetUtils;
import org.primefaces.util.HTML;
import org.primefaces.util.LangUtils;
import org.primefaces.util.WidgetBuilder;

public abstract class BaseMenuRenderer extends MenuItemAwareRenderer {

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        AbstractMenu menu = (AbstractMenu) component;
        MenuModel model = menu.getModel();
        if (model != null && menu.getElementsCount() > 0) {
            model.generateUniqueIds();
        }

        encodeMarkup(context, menu);
        encodeScript(context, menu);
    }

    protected abstract void encodeMarkup(FacesContext context, AbstractMenu abstractMenu) throws IOException;

    protected abstract void encodeScript(FacesContext context, AbstractMenu abstractMenu) throws IOException;

    protected String getLinkStyleClass(MenuItem menuItem) {
        String styleClass = menuItem.getStyleClass();

        return (styleClass == null) ? AbstractMenu.MENUITEM_LINK_CLASS : AbstractMenu.MENUITEM_LINK_CLASS + " " + styleClass;
    }

    protected void encodeMenuItem(FacesContext context, AbstractMenu menu, MenuItem menuitem) throws IOException {
        encodeMenuItem(context, menu, menuitem, "-1");
    }

    protected void encodeMenuItem(FacesContext context, AbstractMenu menu, MenuItem menuitem, String tabindex) throws IOException {
        encodeMenuItem(context, menu, menuitem, tabindex, new SimpleEntry<>(HTML.ARIA_ROLE, HTML.ARIA_ROLE_MENUITEM));
    }

    protected void encodeMenuItem(FacesContext context, AbstractMenu menu, MenuItem menuitem, String tabindex, Entry<String, String> aria) throws IOException {
        boolean isMenuItemComponent = menuitem instanceof UIComponent;
        if (isMenuItemComponent) {
            UIComponent uiMenuItem = (UIComponent) menuitem;
            UIComponent custom = uiMenuItem.getFacet("custom");
            if (FacetUtils.shouldRenderFacet(custom)) {
                custom.encodeAll(context);
                return;
            }
        }

        ResponseWriter writer = context.getResponseWriter();
        String title = menuitem.getTitle();
        String style = menuitem.getStyle();
        boolean disabled = menuitem.isDisabled();
        String rel = menuitem.getRel();
        String ariaLabel = menuitem.getAriaLabel();

        writer.startElement("a", null);
        writer.writeAttribute("tabindex", tabindex, null);
        if (aria != null) {
            writer.writeAttribute(aria.getKey(), aria.getValue(), null);
        }
        if (shouldRenderId(menuitem)) {
            writer.writeAttribute("id", menuitem.getClientId(), null);
        }
        if (title != null) {
            writer.writeAttribute("title", title, null);
        }

        String styleClass = getLinkStyleClass(menuitem);
        if (disabled) {
            styleClass = styleClass + " ui-state-disabled";
        }

        writer.writeAttribute("class", styleClass, null);

        if (style != null) {
            writer.writeAttribute("style", style, null);
        }

        if (rel != null) {
            writer.writeAttribute("rel", rel, null);
        }

        if (LangUtils.isNotEmpty(ariaLabel)) {
            writer.writeAttribute(HTML.ARIA_LABEL, ariaLabel, null);
        }

        if (disabled) {
            writer.writeAttribute("href", "#", null);
            writer.writeAttribute("onclick", "return false;", null);
        }
        else {
            encodeOnClick(context, menu, menuitem);
        }

        if (isMenuItemComponent) {
            renderPassThruAttributes(context, (UIComponent) menuitem);
        }

        encodeMenuItemContent(context, menu, menuitem);

        writer.endElement("a");
    }

    protected boolean shouldRenderId(MenuElement element) {
        if (element instanceof UIComponent) {
            UIComponent component = (UIComponent) element;
            return component.getParent() instanceof MenuButton || shouldWriteId(component);
        }
        else {
            return false;
        }
    }

    protected void encodeMenuItemContent(FacesContext context, AbstractMenu menu, MenuItem menuitem) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        Object value = menuitem.getValue();
        boolean isRtl = ComponentUtils.isRTL(context, menu);
        String iconPos = isRtl ? "right" : menuitem.getIconPos();

        if ("left".equals(iconPos)) {
            encodeIcon(writer, menu, menuitem, iconPos);
        }

        writer.startElement("span", null);
        writer.writeAttribute("class", AbstractMenu.MENUITEM_TEXT_CLASS, null);

        if (menuitem.shouldRenderChildren()) {
            renderChildren(context, (UIComponent) menuitem);
        }
        else if (value != null) {
            if (menuitem.isEscape()) {
                writer.writeText(value, "value");
            }
            else {
                writer.write(value.toString());
            }
        }

        writer.endElement("span");

        if ("right".equals(iconPos)) {
            encodeIcon(writer, menu, menuitem, iconPos);
        }
    }

    protected void encodeIcon(ResponseWriter writer, AbstractMenu menu, MenuItem menuitem, String iconPos) throws IOException {
        String icon = menuitem.getIcon();
        if (icon != null && LangUtils.isNotBlank(iconPos)) {
            writer.startElement("span", null);
            writer.writeAttribute("class", AbstractMenu.MENUITEM_ICON_CLASS + " " + icon + " ui-menuitem-icon-" + iconPos, null);
            writer.writeAttribute(HTML.ARIA_HIDDEN, "true", null);
            writer.endElement("span");
        }
    }

    protected void encodeOverlayConfig(FacesContext context, OverlayMenu menu, WidgetBuilder wb) throws IOException {
        wb.attr("overlay", true)
                .attr("my", menu.getMy())
                .attr("at", menu.getAt());

        String trigger = menu.getTrigger();
        if (LangUtils.isNotBlank(trigger)) {
            wb.attr("trigger", SearchExpressionUtils.resolveClientIdsForClientSide(context, (UIComponent) menu, trigger))
                .attr("triggerEvent", menu.getTriggerEvent());
        }
    }

    @Override
    public void encodeChildren(FacesContext facesContext, UIComponent component) throws IOException {
        //Do nothing
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    protected void encodeKeyboardTarget(FacesContext context, AbstractMenu menu) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        writer.startElement("div", null);
        writer.writeAttribute("tabindex", menu.getTabindex(), null);
        writer.writeAttribute("class", "ui-helper-hidden-accessible", null);
        writer.endElement("div");
    }
}
