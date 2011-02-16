/* ===========================================================
 * AFreeChart : a free chart library for Android(tm) platform.
 *              (based on JFreeChart and JCommon)
 * ===========================================================
 *
 * (C) Copyright 2010, by Icom Systech Co., Ltd.
 * (C) Copyright 2000-2009, by Object Refinery Limited and Contributors.
 *
 * Project Info:
 *    AFreeChart: http://code.google.com/p/afreechart/
 *    JFreeChart: http://www.jfree.org/jfreechart/index.html
 *    JCommon   : http://www.jfree.org/jcommon/index.html
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * [Android is a trademark of Google Inc.]
 *
 * ----------------------
 * YIntervalRenderer.java
 * ----------------------
 * 
 * (C) Copyright 2010, by Icom Systech Co., Ltd.
 *
 * Original Author:  shiraki  (for Icom Systech Co., Ltd);
 * Contributor(s):   Sato Yoshiaki ;
 *                   Niwano Masayoshi;
 *
 * Changes (from 19-Nov-2010)
 * --------------------------
 * 19-Nov-2010 : port JFreeChart 1.0.13 to Android as "AFreeChart"
 * 
 * ------------- JFreeChart ---------------------------------------------
 * (C) Copyright 2002-2009, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * Changes
 * -------
 * 05-Nov-2002 : Version 1 (DG);
 * 25-Mar-2003 : Implemented Serializable (DG);
 * 01-May-2003 : Modified drawItem() method signature (DG);
 * 20-Aug-2003 : Implemented Cloneable and PublicCloneable (DG);
 * 16-Sep-2003 : Changed ChartRenderingInfo --> PlotRenderingInfo (DG);
 * 25-Feb-2004 : Replaced CrosshairInfo with CrosshairState (DG);
 * 27-Sep-2004 : Access double values from dataset (DG);
 * 11-Nov-2004 : Now uses ShapeUtilities to translate shapes (DG);
 * 11-Apr-2008 : New override for findRangeBounds() (DG);
 * 26-May-2008 : Added item label support (DG);
 * 27-Mar-2009 : Updated findRangeBounds() (DG);
 *
 */

package org.afree.chart.renderer.xy;

import java.io.Serializable;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

import org.afree.util.ObjectUtilities;
import org.afree.util.PublicCloneable;
import org.afree.ui.RectangleEdge;
import org.afree.util.ShapeUtilities;
import org.afree.chart.axis.ValueAxis;
import org.afree.data.xy.IntervalXYDataset;
import org.afree.data.Range;
import org.afree.data.xy.XYDataset;
import org.afree.chart.entity.EntityCollection;
import org.afree.chart.labels.ItemLabelPosition;
import org.afree.chart.labels.XYItemLabelGenerator;
import org.afree.chart.plot.CrosshairState;
import org.afree.chart.plot.PlotOrientation;
import org.afree.chart.plot.PlotRenderingInfo;
import org.afree.chart.plot.XYPlot;
import org.afree.chart.text.TextUtilities;
import org.afree.graphics.geom.LineShape;
import org.afree.graphics.geom.RectShape;
import org.afree.graphics.geom.Shape;
import org.afree.graphics.PaintUtility;


/**
 * A renderer that draws a line connecting the start and end Y values for an
 * {@link XYPlot}.  The example shown here is generated by the
 * <code>YIntervalRendererDemo1.java</code> program included in the AFreeChart
 * demo collection:
 * <br><br>
 * <img src="../../../../../images/YIntervalRendererSample.png"
 * alt="YIntervalRendererSample.png" />
 */
public class YIntervalRenderer extends AbstractXYItemRenderer
        implements XYItemRenderer, Cloneable, PublicCloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -2951586537224143260L;

    /**
     * An additional item label generator.  If this is non-null, the item
     * label generated will be displayed near the lower y-value at the
     * position given by getNegativeItemLabelPosition().
     *
     * @since JFreeChart 1.0.10
     */
    private XYItemLabelGenerator additionalItemLabelGenerator;

    /**
     * The default constructor.
     */
    public YIntervalRenderer() {
        super();
        this.additionalItemLabelGenerator = null;
    }

    /**
     * Returns the generator for the item labels that appear near the lower
     * y-value.
     *
     * @return The generator (possibly <code>null</code>).
     *
     * @see #setAdditionalItemLabelGenerator(XYItemLabelGenerator)
     *
     * @since JFreeChart 1.0.10
     */
    public XYItemLabelGenerator getAdditionalItemLabelGenerator() {
        return this.additionalItemLabelGenerator;
    }

    /**
     * Sets the generator for the item labels that appear near the lower
     * y-value and sends a {@link RendererChangeEvent} to all registered
     * listeners.  If this is set to <code>null</code>, no item labels will be
     * drawn.
     *
     * @param generator  the generator (<code>null</code> permitted).
     *
     * @see #getAdditionalItemLabelGenerator()
     *
     * @since JFreeChart 1.0.10
     */
    public void setAdditionalItemLabelGenerator(
            XYItemLabelGenerator generator) {
        this.additionalItemLabelGenerator = generator;
//        fireChangeEvent();
    }

    /**
     * Returns the range of values the renderer requires to display all the
     * items from the specified dataset.
     *
     * @param dataset  the dataset (<code>null</code> permitted).
     *
     * @return The range (<code>null</code> if the dataset is <code>null</code>
     *         or empty).
     */
    public Range findRangeBounds(XYDataset dataset) {
        return findRangeBounds(dataset, true);
    }

    /**
     * Draws the visual representation of a single data item.
     *
     * @param canvas  the graphics device.
     * @param state  the renderer state.
     * @param dataArea  the area within which the plot is being drawn.
     * @param info  collects information about the drawing.
     * @param plot  the plot (can be used to obtain standard color
     *              information etc).
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset.
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     * @param crosshairState  crosshair information for the plot
     *                        (<code>null</code> permitted).
     * @param pass  the pass index (ignored here).
     */
    public void drawItem(Canvas canvas,
                         XYItemRendererState state,
                         RectShape dataArea,
                         PlotRenderingInfo info,
                         XYPlot plot,
                         ValueAxis domainAxis,
                         ValueAxis rangeAxis,
                         XYDataset dataset,
                         int series,
                         int item,
                         CrosshairState crosshairState,
                         int pass) {

        // setup for collecting optional entity info...
        EntityCollection entities = null;
        if (info != null) {
            entities = info.getOwner().getEntityCollection();
        }

        IntervalXYDataset intervalDataset = (IntervalXYDataset) dataset;

        double x = intervalDataset.getXValue(series, item);
        double yLow   = intervalDataset.getStartYValue(series, item);
        double yHigh  = intervalDataset.getEndYValue(series, item);

        RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        RectangleEdge yAxisLocation = plot.getRangeAxisEdge();

        double xx = domainAxis.valueToJava2D(x, dataArea, xAxisLocation);
        double yyLow = rangeAxis.valueToJava2D(yLow, dataArea, yAxisLocation);
        double yyHigh = rangeAxis.valueToJava2D(yHigh, dataArea, yAxisLocation);

        LineShape line = null;
        Shape shape = getItemShape(series, item);
        Shape top = null;
        Shape bottom = null;
        PlotOrientation orientation = plot.getOrientation();
        if (orientation == PlotOrientation.HORIZONTAL) {
            line = new LineShape(yyLow, xx, yyHigh, xx);
            top = ShapeUtilities.createTranslatedShape(shape, yyHigh, xx);
            bottom = ShapeUtilities.createTranslatedShape(shape, yyLow, xx);
        }
        else if (orientation == PlotOrientation.VERTICAL) {
            line = new LineShape(xx, yyLow, xx, yyHigh);
            top = ShapeUtilities.createTranslatedShape(shape, xx, yyHigh);
            bottom = ShapeUtilities.createTranslatedShape(shape, xx, yyLow);
        }
        
        Paint paint = PaintUtility.createPaint(
                Paint.ANTI_ALIAS_FLAG,
                getItemPaintType(series, item),
                getItemStroke(series, item),
                getItemEffect(series, item));
        line.draw(canvas, paint);
        
        top.fill(canvas, paint);
        bottom.fill(canvas, paint);

        // for item labels, we have a special case because there is the
        // possibility to draw (a) the regular item label near to just the
        // upper y-value, or (b) the regular item label near the upper y-value
        // PLUS an additional item label near the lower y-value.
        if (isItemLabelVisible(series, item)) {
            drawItemLabel(canvas, orientation, dataset, series, item, xx, yyHigh,
                    false);
            drawAdditionalItemLabel(canvas, orientation, dataset, series, item,
                    xx, yyLow);
        }

        // add an entity for the item...
        if (entities != null) {
            RectShape rectShape = new RectShape();
            line.getBounds(rectShape);
            addEntity(entities, rectShape, dataset, series, item, 0.0,
                    0.0);
        }

    }

    /**
     * Draws an item label.
     *
     * @param canvas  the graphics device.
     * @param orientation  the orientation.
     * @param dataset  the dataset.
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     * @param x  the x coordinate (in Java2D space).
     * @param y  the y coordinate (in Java2D space).
     * @param negative  indicates a negative value (which affects the item
     *                  label position).
     */
    private void drawAdditionalItemLabel(Canvas canvas,
            PlotOrientation orientation, XYDataset dataset, int series,
            int item, double x, double y) {

        if (this.additionalItemLabelGenerator == null) {
            return;
        }
        
        String label = this.additionalItemLabelGenerator.generateLabel(dataset,
                series, item);

        ItemLabelPosition position = getNegativeItemLabelPosition(series, item);
        PointF anchorPoint = calculateLabelAnchorPoint(
                position.getItemLabelAnchor(), x, y, orientation);
        
        Paint paint = PaintUtility.createPaint(
                Paint.ANTI_ALIAS_FLAG,
                getItemLabelPaintType(series, item),
                getItemLabelFont(series, item));
        
        TextUtilities.drawRotatedString(label, canvas,
                (float) anchorPoint.x, (float) anchorPoint.y,
                position.getTextAnchor(), position.getAngle(),
                position.getRotationAnchor(), paint);
    }

    /**
     * Tests this renderer for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof YIntervalRenderer)) {
            return false;
        }
        YIntervalRenderer that = (YIntervalRenderer) obj;
        if (!ObjectUtilities.equal(this.additionalItemLabelGenerator,
                that.additionalItemLabelGenerator)) {
            return false;
        }
        return super.equals(obj);
    }

    /**
     * Returns a clone of the renderer.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException  if the renderer cannot be cloned.
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}