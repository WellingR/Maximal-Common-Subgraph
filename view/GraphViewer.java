package view;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JApplet;
import javax.swing.JFrame;
import org.jgraph.JGraph;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphModelAdapter;

/**
 *
 * @author Ruud
 */
public class GraphViewer<V,E> extends JApplet {

	private static final long serialVersionUID = 2474107909791138543L;
	private static final Color DEFAULT_BG_COLOR = Color.decode("#FAFBFF");
    private static final Dimension DEFAULT_SIZE = new Dimension(500, 400);

    
    private JGraphModelAdapter<V,E> jgAdapter;

    private GraphViewer(Graph<V,E> g){

        // create a visualization using JGraph, via an adapter
        jgAdapter = new JGraphModelAdapter<V,E>(g);

        
    }

    public static <V,E> void showGraph(Graph<V,E> g, String title){
        GraphViewer<V,E> applet = new GraphViewer<V,E>(g);
        applet.init();

        JFrame frame = new JFrame();
        frame.getContentPane().add(applet);
        frame.setTitle(title);
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public void init()
    {
        JGraph jgraph = new JGraph(jgAdapter);

        adjustDisplaySettings(jgraph);
        getContentPane().add(jgraph);
        resize(DEFAULT_SIZE);
    }

        private void adjustDisplaySettings(JGraph jg)
    {
        jg.setPreferredSize(DEFAULT_SIZE);

        Color c = DEFAULT_BG_COLOR;
        String colorStr = null;

        try {
            colorStr = getParameter("bgcolor");
        } catch (Exception e) {
        }

        if (colorStr != null) {
            c = Color.decode(colorStr);
        }

        jg.setBackground(c);
    }

}
