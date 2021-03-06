package org.jxls.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.jxls.area.Area;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.common.Size;
import org.jxls.util.UtilWrapper;

/**
 * The command implements a grid with dynamic columns and rows
 * 
 * @author Leonid Vysochyn
 */
public class GridCommand extends AbstractCommand {
    public static final String COMMAND_NAME = "grid";
    public static final String HEADER_VAR = "header";
    public static final String DATA_VAR = "cell";

    /** Name of a context variable containing a collection of headers */
    private String headers;
    /** Name of a context variable containing a collection of data objects for body */
    private String data;
    /** Comma separated list of object property names for each grid row */
    private String props;
    /** Comma separated list of format type cells, e.g. formatCells="Double:E1, Date:F1" */
    private String formatCells;
    private Map<String,String> cellStyleMap = new HashMap<>();
    private List<String> rowObjectProps = new ArrayList<>();
    private Area headerArea;
    private Area bodyArea;
    private UtilWrapper util = new UtilWrapper();

    public GridCommand() {
    }

    public GridCommand(String headers, String data) {
        this.headers = headers;
        this.data = data;
    }

    public GridCommand(String headers, String data, String props, Area headerArea, Area bodyArea) {
        this(headers, data, headerArea, bodyArea);
        this.props = props;
    }

    public GridCommand(String headers, String data, Area headerArea, Area bodyArea) {
        this.headers = headers;
        this.data = data;
        this.headerArea = headerArea;
        this.bodyArea = bodyArea;
        addArea(headerArea);
        addArea(bodyArea);
    }

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getProps() {
        return props;
    }

    public void setProps(String props) {
        this.props = props;
        if (props != null) {
            rowObjectProps = Arrays.asList(props.replaceAll("\\s+", "").split(",")); // Remove whitespace and split into List.
        }
    }

    public String getFormatCells() {
        return formatCells;
    }

    /**
     * @param formatCells Comma-separated list of format type cells, e.g. formatCells="Double:E1, Date:F1"
     */
    public void setFormatCells(String formatCells) {
        this.formatCells = formatCells;
        if (formatCells == null) {
            return;
        }
        List<String> cellStyleList = Arrays.asList(formatCells.split(","));
        for (String cellStyleString : cellStyleList) {
            String[] styleCell = cellStyleString.split(":");
            cellStyleMap.put(styleCell[0].trim(), styleCell[1].trim());
        }
    }

    @Override
    public Command addArea(Area area) {
        if (areaList.size() >= 2) {
            throw new IllegalArgumentException("Cannot add any more areas to GridCommand. You can add only 1 area as a 'header' and 1 area as a 'body'.");
        }
        if (areaList.isEmpty()) {
            headerArea = area;
        } else {
            bodyArea = area;
        }
        return super.addArea(area);
    }

    @Override
    public Size applyAt(CellRef cellRef, Context context) {
        Size headerAreaSize = processHeaders(cellRef, context);
        CellRef bodyCellRef = new CellRef(cellRef.getSheetName(), cellRef.getRow() + headerAreaSize.getHeight(), cellRef.getCol());
        Size bodyAreaSize = processBody(bodyCellRef, context);
        int gridHeight = headerAreaSize.getHeight() + bodyAreaSize.getHeight();
        int gridWidth = Math.max(headerAreaSize.getWidth(), bodyAreaSize.getWidth());
        return new Size(gridWidth, gridHeight);
    }

    private Size processHeaders(CellRef cellRef, Context context) {
        if (headerArea == null || headers == null) {
            return Size.ZERO_SIZE;
        }
        Iterable<?> headers = util.transformToIterableObject(getTransformationConfig().getExpressionEvaluator(), this.headers, context);
        CellRef currentCell = cellRef;
        int width = 0;
        int height = 0;
        // TODO possible error: content of HEADER_VAR is not saved & restored
        for (Object header : headers) {
            context.putVar(HEADER_VAR, header);
            Size size = headerArea.applyAt(currentCell, context);
            currentCell = new CellRef(currentCell.getSheetName(), currentCell.getRow(), currentCell.getCol() + size.getWidth());
            width += size.getWidth();
            height = Math.max(height, size.getHeight());
        }
        context.removeVar(HEADER_VAR);
    
        return new Size(width, height);
    }

    // TODO method too long, similar code
    private Size processBody(final CellRef cellRef, Context context) {
        if (bodyArea == null || data == null) {
            return Size.ZERO_SIZE;
        }
        Iterable<?> dataCollection = util.transformToIterableObject(getTransformationConfig().getExpressionEvaluator(), this.data, context);

        CellRef currentCell = cellRef;
        int totalWidth = 0;
        int totalHeight = 0;
        Context.Config config = context.getConfig();
        boolean oldIgnoreSourceCellStyle = config.isIgnoreSourceCellStyle();
        config.setIgnoreSourceCellStyle(true);
        Map<String,String> oldStyleCellMap = config.getCellStyleMap();
        config.setCellStyleMap(this.cellStyleMap);
        // TODO possible error: content of DATA_VAR is not saved & restored
        for (Object rowObject : dataCollection) {
            if (rowObject.getClass().isArray() || rowObject instanceof Iterable) {
                Iterable<?> cellCollection = null;
                if (rowObject.getClass().isArray()) {
                    cellCollection = Arrays.asList((Object[]) rowObject);
                } else {
                    cellCollection = (Iterable<?>) rowObject;
                }
                int width = 0;
                int height = 0;
                for (Object cellObject : cellCollection) {
                    context.putVar(DATA_VAR, cellObject);
                    Size size = bodyArea.applyAt(currentCell, context);
                    currentCell = new CellRef(currentCell.getSheetName(), currentCell.getRow(), currentCell.getCol() + size.getWidth());
                    width += size.getWidth();
                    height = Math.max(height, size.getHeight());
                }
                totalWidth = Math.max(width, totalWidth);
                totalHeight = totalHeight + height;
                currentCell = new CellRef(cellRef.getSheetName(), currentCell.getRow() + height, cellRef.getCol());
            } else {
                if (rowObjectProps.isEmpty()) {
                    throw new IllegalArgumentException("Got a non-collection object type for a Grid row but object properties list is empty");
                }
                int width = 0;
                int height = 0;
                for (String prop : rowObjectProps) {
                    try {
                        Object value = PropertyUtils.getProperty(rowObject, prop);
                        context.putVar(DATA_VAR, value);
                        Size size = bodyArea.applyAt(currentCell, context);
                        currentCell = new CellRef(currentCell.getSheetName(), currentCell.getRow(), currentCell.getCol() + size.getWidth());
                        width += size.getWidth();
                        height = Math.max(height, size.getHeight());
                    } catch (Exception e) {
                        String message = "Failed to evaluate property " + prop + " of row object of class " + rowObject.getClass().getName();
                        throw new IllegalStateException(message, e);
                    }
                }
                totalWidth = Math.max(width, totalWidth);
                totalHeight = totalHeight + height;
                currentCell = new CellRef(cellRef.getSheetName(), currentCell.getRow() + height, cellRef.getCol());
            }
        }
        context.removeVar(DATA_VAR);
        config.setIgnoreSourceCellStyle(oldIgnoreSourceCellStyle);
        config.setCellStyleMap(oldStyleCellMap);
        return new Size(totalWidth, totalHeight);
    }
}
