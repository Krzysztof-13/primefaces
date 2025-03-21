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
package org.primefaces.component.charts;

import java.io.IOException;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.primefaces.component.api.UIChart;
import org.primefaces.context.PrimeRequestContext;
import org.primefaces.model.charts.ChartData;
import org.primefaces.model.charts.ChartDataSet;
import org.primefaces.model.charts.ChartModel;
import org.primefaces.model.charts.ChartOptions;
import org.primefaces.model.charts.axes.cartesian.CartesianAxes;
import org.primefaces.model.charts.axes.cartesian.CartesianScales;
import org.primefaces.model.charts.axes.radial.RadialScales;
import org.primefaces.model.charts.optionconfig.animation.Animation;
import org.primefaces.model.charts.optionconfig.elements.Elements;
import org.primefaces.model.charts.optionconfig.legend.Legend;
import org.primefaces.model.charts.optionconfig.title.Title;
import org.primefaces.model.charts.optionconfig.tooltip.Tooltip;
import org.primefaces.renderkit.CoreRenderer;
import org.primefaces.util.ChartUtils;
import org.primefaces.util.EscapeUtils;
import org.primefaces.util.HTML;
import org.primefaces.util.LangUtils;

public class ChartRenderer extends CoreRenderer {

    @Override
    public void decode(FacesContext context, UIComponent component) {
        super.decodeBehaviors(context, component);
    }

    protected void encodeMarkup(FacesContext context, UIChart chart) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        String clientId = chart.getClientId(context);
        String style = chart.getStyle();
        String styleClass = chart.getStyleClass();
        styleClass = (styleClass != null) ? "ui-chart " + styleClass : "ui-chart";

        writer.startElement("div", null);
        writer.writeAttribute("id", clientId, null);
        writer.writeAttribute("class", styleClass, "styleClass");

        writer.startElement("canvas", null);
        writer.writeAttribute("id", clientId + "_canvas", null);
        writer.writeAttribute(HTML.ARIA_ROLE, "img", null);
        String ariaLabel = chart.getAriaLabel();
        if (LangUtils.isBlank(ariaLabel)) {
            ChartOptions options = (ChartOptions) chart.getModel().getOptions();
            if (options != null) {
                Title title = options.getTitle();
                if (title != null) {
                    ariaLabel = String.valueOf(title.getText());
                }
            }
        }
        writer.writeAttribute(HTML.ARIA_LABEL, ariaLabel, null);
        if (style != null) writer.writeAttribute("style", style, "style");
        writer.endElement("canvas");

        writer.endElement("div");
    }

    protected void encodeConfig(FacesContext context, ChartModel model) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        ChartData data = model.getData();
        Object options = model.getOptions();

        writer.write(",\"config\":{");

        writer.write("\"type\":\"" + model.getType() + "\"");
        encodeData(context, data);
        encodeOptions(context, model.getType(), options);

        writer.write("}");

        String extender = model.getExtender();
        if (extender != null) {
            writer.write(",\"extender\":" + extender);
        }
    }

    protected void encodeData(FacesContext context, ChartData data) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        if (data == null) {
            return;
        }

        List<ChartDataSet> dataSetList = data.getDataSet();

        writer.write(",\"data\":{");

        writer.write("\"datasets\":[");

        for (int i = 0; i < dataSetList.size(); i++) {
            ChartDataSet dataSet = dataSetList.get(i);

            if (dataSet != null) {
                if (i != 0) {
                    writer.write(",");
                }

                writer.write(dataSet.encode());
            }
        }

        writer.write("]");

        Object labels = data.getLabels();
        if (labels != null) {
            writer.write(",\"labels\":");
            writeLabels(context, labels);
        }

        writer.write("}");
    }

    protected void writeLabels(FacesContext context, Object labels) throws IOException {
        boolean isList = labels instanceof List;

        if (isList) {
            ResponseWriter writer = context.getResponseWriter();
            List<?> labelList = (List<?>) labels;

            writer.write("[");
            for (int i = 0; i < labelList.size(); i++) {
                if (i != 0) {
                    writer.write(",");
                }

                Object item = labelList.get(i);
                if (item instanceof String) {
                    writer.write("\"" + EscapeUtils.forJavaScript((String) item) + "\"");
                }
                else {
                    writeLabels(context, item);
                }
            }
            writer.write("]");
        }
    }

    protected void encodeOptions(FacesContext context, String type, Object options) throws IOException {
        // implemented by chart components
    }

    protected void encodeScales(FacesContext context, String chartName, Object scales, boolean hasComma) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        if (scales != null) {
            if (hasComma) {
                writer.write(",");
            }

            if (scales instanceof CartesianScales) {
                writer.write("\"scales\":{");
                CartesianScales cScales = (CartesianScales) scales;
                StringBuilder scaleAttrs = new StringBuilder(128);

                List<CartesianAxes> xAxes = cScales.getXAxes();
                if (xAxes != null && !xAxes.isEmpty()) {
                    encodeAxes(context, chartName, "x", xAxes);
                }

                List<CartesianAxes> yAxes = cScales.getYAxes();
                if (yAxes != null && !yAxes.isEmpty()) {
                    if (xAxes != null && !xAxes.isEmpty()) {
                        writer.write(",");
                    }
                    encodeAxes(context, chartName, "y", yAxes);
                }

                writer.write(scaleAttrs.toString());
                writer.write("}");
            }
            else if (scales instanceof RadialScales) {
                writer.write("\"scale\":{");
                RadialScales rScales = (RadialScales) scales;
                StringBuilder scaleAttrs = new StringBuilder(128);
                if (rScales.getAngleLines() != null) {
                    writeJsonAttribute(scaleAttrs, "angleLines", rScales.getAngleLines().encode());
                }

                if (rScales.getGridLines() != null) {
                    writeJsonAttribute(scaleAttrs, "grid", rScales.getGridLines().encode());
                }

                if (rScales.getPointLabels() != null) {
                    writeJsonAttribute(scaleAttrs, "pointLabels", rScales.getPointLabels().encode());
                }

                if (rScales.getTicks() != null) {
                    writeJsonAttribute(scaleAttrs, "ticks", rScales.getTicks().encode());
                }

                if (rScales.getStartAngle() != null) {
                    writeJsonAttribute(scaleAttrs, "startAngle", rScales.getStartAngle().toString());
                }

                writer.write(scaleAttrs.toString());
                writer.write("}");
            }
        }
    }

    private void writeJsonAttribute(StringBuilder stringBuilder, String attributeName, String attributeValue) {
        if (stringBuilder.length() > 0) {
            stringBuilder.append(",");
        }
        stringBuilder.append("\"" + attributeName + "\":" + attributeValue);
    }

    protected void encodeAxes(FacesContext context, String chartName, String defaultAxeId, List<CartesianAxes> axes) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        for (int i = 0; i < axes.size(); i++) {
            if (i > 0) {
                writer.write(",");
            }

            CartesianAxes data = axes.get(i);
            String axeId = data.getId() == null ? defaultAxeId : data.getId();
            writer.write("\"" + axeId + "\": {");
            writer.write(data.encode());
            writer.write("}");
        }
    }

    protected void encodeElements(FacesContext context, Elements elements, boolean hasComma) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        if (elements != null) {
            if (hasComma) {
                writer.write(",");
            }

            writer.write("\"elements\":{");
            writer.write(elements.encode());
            writer.write("}");
        }
    }

    protected void encodeResponsive(FacesContext context, ChartOptions options, boolean hasComma) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        if (hasComma) {
            writer.write(",");
        }

        ChartUtils.writeDataValue(writer, "responsive", options.isResponsive(), false);
        ChartUtils.writeDataValue(writer, "maintainAspectRatio", options.isMaintainAspectRatio(), true);
        ChartUtils.writeDataValue(writer, "aspectRatio", options.getAspectRatio(), true);
    }

    protected void encodePlugins(FacesContext context, ChartOptions options, boolean hasComma) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        if (hasComma) {
            writer.write(",");
        }

        writer.write("\"plugins\":{");
        Title title = options.getTitle();
        Title subtitle = options.getSubtitle();
        Tooltip tooltip = options.getTooltip();
        Legend legend = options.getLegend();

        encodeTitle(context, title, "title", false);
        encodeTitle(context, subtitle, "subtitle", title != null);
        encodeTooltip(context, tooltip, title != null || subtitle != null);
        encodeLegend(context, legend, title != null || subtitle != null || tooltip != null);
        writer.write("}");
    }

    protected void encodeTitle(FacesContext context, Title title, String element, boolean hasComma) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        if (title != null) {
            if (hasComma) {
                writer.write(",");
            }

            writer.write(String.format("\"%s\":{", element));
            writer.write(title.encode());
            writer.write("}");
        }
    }

    protected void encodeTooltip(FacesContext context, Tooltip tooltip, boolean hasComma) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        if (tooltip != null) {
            if (hasComma) {
                writer.write(",");
            }

            if (PrimeRequestContext.getCurrentInstance(context).isRTL()) {
                tooltip.setRtl(true);
                tooltip.setTextDirection("rtl");
            }

            writer.write("\"tooltip\":{");
            writer.write(tooltip.encode());
            writer.write("}");
        }
    }

    protected void encodeLegend(FacesContext context, Legend legend, boolean hasComma) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        if (legend != null) {
            if (hasComma) {
                writer.write(",");
            }

            if (PrimeRequestContext.getCurrentInstance(context).isRTL()) {
                legend.setRtl(true);
                legend.setTextDirection("rtl");
            }

            writer.write("\"legend\":{");
            writer.write(legend.encode());
            writer.write("}");
        }
    }

    protected void encodeAnimation(FacesContext context, Animation animation, boolean hasComma) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        if (animation != null) {
            if (hasComma) {
                writer.write(",");
            }

            writer.write("\"animation\":{");
            writer.write(animation.encode());
            writer.write("}");
        }
    }
}
