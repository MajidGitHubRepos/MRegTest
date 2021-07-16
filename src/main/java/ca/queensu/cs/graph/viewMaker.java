package ca.queensu.cs.graph;


import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.w3c.dom.Node;

import com.mxgraph.io.mxCodec;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.util.mxCellRenderer;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxGraph;

public class viewMaker
{

	public static void main(String[] args)
	{
		// Creates graph with model
		mxGraph graph = new mxGraph();
		Object parent = graph.getDefaultParent();
		
		// Sets the default vertex style
		Map<String, Object> style = graph.getStylesheet().getDefaultVertexStyle();
		style.put(mxConstants.STYLE_GRADIENTCOLOR, "#FFFFFF");
		style.put(mxConstants.STYLE_ROUNDED, true);
		style.put(mxConstants.STYLE_SHADOW, true);

		graph.getModel().beginUpdate();
		try
		{
			String startState = "ellipse;html=1;shape=startState;fillColor=#000000;strokeColor=#ff0000;rounded=1;shadow=0;comic=0;labelBackgroundColor=none;fontFamily=Verdana;fontSize=12;fontColor=#000000;align=center;direction=south;";
			Object v1 = graph.insertVertex(parent, null, null, 130, 135, 30, 30,startState);
			
			String state = "rounded=1;whiteSpace=wrap;html=1;arcSize=24;fillColor=#ffffc0;strokeColor=#ff0000;shadow=0;comic=0;labelBackgroundColor=none;fontFamily=Verdana;fontSize=12;fontColor=#000000;align=center;";

			Object v2 = graph.insertVertex(parent, null, "v1", 240, 310, 120, 60, state);
			
			String edgeStyle = "edgeStyle=orthogonalEdgeStyle;html=1;exitX=1;exitY=0.25;entryX=0.25;entryY=0;labelBackgroundColor=none;endArrow=open;endSize=8;strokeColor=#ff0000;fontFamily=Verdana;fontSize=12;align=left;";
			
			graph.insertEdge(parent, null, "e1", v1, v2, edgeStyle);
			
			String containerStyle = "swimlane;whiteSpace=wrap;html=1;rounded=1;shadow=0;comic=0;labelBackgroundColor=none;strokeColor=#000000;strokeWidth=1;fillColor=#ffffff;fontFamily=Verdana;fontSize=12;fontColor=#000000;align=center;";
			
			Object container = graph.insertVertex(parent, null, "Container", 95, 495, 930, 200, containerStyle);
			//Object rectangle = graph.insertVertex(container, null, null, 95, 495, 80, 23);
			
			//Object v3 = graph.insertVertex(container, null, null, 75, 80, 30, 30,startState); //0,0 is fine because it has parent!
			//Object v4 = graph.insertVertex(container, null, "v4", 215, 65, 120, 60, state);
			
			Object v3 = graph.insertVertex(container, null, null, 0, 0, 30, 30,startState); //0,0 is fine because it has parent!
			Object v4 = graph.insertVertex(container, null, "v4", 0, 0, 120, 60, state);
			//graph.insertEdge(container, null, "e2", v3, v4, edgeStyle);
			mxIGraphLayout layout = new mxHierarchicalLayout(graph);
			layout.execute(parent);
		}
		finally
		{
			graph.getModel().endUpdate();
		}
		
		
		
		mxCodec codec = new mxCodec();
		Node node = codec.encode(graph.getModel());
		System.out.println("xml=" + mxUtils.getPrettyXml(node));
		
		try (FileOutputStream fos = new FileOutputStream("test.xml")) {
            
            String text = mxUtils.getPrettyXml(node);
            byte[] mybytes = text.getBytes();
            
            fos.write(mybytes);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		// Creates an image than can be saved using ImageIO
		BufferedImage image = mxCellRenderer.createBufferedImage(graph, null,
				1, Color.WHITE, true, null);

		// For the sake of this example we display the image in a window
		JFrame frame = new JFrame("Graph image");
		frame.getContentPane().add(new JLabel(new ImageIcon(image)));
		frame.pack();
		frame.setVisible(true);
	}

}