package viewer2D.graphic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import javax.swing.JComponent;

import viewer2D.controler.WorldModelListener;
import viewer2D.data.Camera;
import viewer2D.data.Viewport;
import viewer2D.data.WorldModel;
import viewer2D.geometry.Shape2D;
import math2D.Base2D;
import math2D.Transformation2D;
import math2D.Point2D;
import math2D.Vecteur2D;

public class Viewer2D extends JComponent {
	
	private static final long serialVersionUID = 1L;
	public static final String MODEL_CHANGED_PROPERTY = "model";
	
	private WorldModel model;
	private Camera camera;
	private Viewport viewport;
	private Handler handler;
	
	private BasicStroke gridStroke, axisStroke;
	private Transformation2D screenMVP;
	private int lineClicked = 0;
	private int columnClicked = 0;
	
	private int unityGrid = 1;
	private int eventButton = 0;
	private boolean movable = true;
	private boolean spinnable = true;
	private boolean zoomable = true;
	
	/** Contructeur */
	public Viewer2D(WorldModel model, int width, int height) {
		super();
		this.model = null;
		this.camera = new Camera();
		this.viewport = new Viewport(0, 0, width, height);
		this.handler = new Handler();
		
		setModel(model);
		
		this.gridStroke = new BasicStroke();
		this.axisStroke = new BasicStroke(3.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL);
		
		this.addMouseListener(getHandler());
		this.addMouseMotionListener(getHandler());
		this.addMouseWheelListener(getHandler());
		this.addComponentListener(getHandler());
		
		this.setPreferredSize(new Dimension(width, height));
	}
	
	/** Contructeur */
	public Viewer2D(int width, int height) {
		this(new WorldModel(), width, height);
	}
	
	/** Contructeur */
	public Viewer2D() {
		this(640, 480);
	}
	
	/** Retourne le modele */
	public WorldModel getModel() {
		return model;
	}
	
	/** Retourne le handler sur le Viewer2D */
	private Handler getHandler() {
		return handler;
	}
	
	/** Met a jour le modele */
	public void setModel(WorldModel newModel) {
		WorldModel oldModel = getModel();
		
		if (oldModel != null) {
			oldModel.removeWorldListener(getHandler());
		}
		
		model = newModel;
		
		if (newModel != null) {
			newModel.addWorldListener(getHandler());
		}
		
		firePropertyChange(MODEL_CHANGED_PROPERTY, oldModel, model);
	}
	
	
	/** Retourne l'unite de la grile */
	public int getUnity() {
		return unityGrid;
	}
	
	/** Indique si la camera est translatable */
	public boolean getMoveable() {
		return movable;
	}
	
	/** Indique si la camera est tournable */
	public boolean getSpinnable() {
		return spinnable;
	}
	
	/** Indique si la camera peut zoomer */
	public boolean getZoomable() {
		return zoomable;
	}
	
//	/** Ajoute une Shaped2D a afficher dans le viewer */
//	public void addShape(Shape2D shape) {
//		listShape.add(shape);
//	}
//	
//	/** Retire une Shaped2D du viewer */
//	public void removeShape(Shape2D shape) {
//		listShape.remove(shape);
//	}
	
	/** Met a jour l'unite de la grille */
	public void setUnity(int value) {
		unityGrid = Math.max(1, value);
	}
	
	/** Permet la translation de la camera */
	public void setMoveable(boolean value) {
		this.movable = value;
	}
	
	/** Permet la rotation de la camera */
	public void setSpinnable(boolean value) {
		this.spinnable = value;
	}
	
	/** Permet le zoom de la camera */
	public void setZoomable(boolean value) {
		this.zoomable = value;
	}
	
	public void drawPoint(Graphics g2, Point2D point) {
		Point2D proj_p = screenMVP.transform(point);
		g2.fillOval((int) proj_p.getX() - 4, (int) proj_p.getY() - 4, 8, 8);
	}
	
	public void drawLine(Graphics g2, Point2D point1, Point2D point2) {
		Point2D proj_p1 = screenMVP.transform(point1);
		Point2D proj_p2 = screenMVP.transform(point2);
		g2.drawLine((int) proj_p1.getX(), (int) proj_p1.getY(), (int) proj_p2.getX(), (int) proj_p2.getY());
	}
	
	public void drawArrow(Graphics g2, Point2D point1, Point2D point2) {
		Point2D proj_p;
		Point2D proj_p1 = screenMVP.transform(point1);
		Point2D proj_p2 = screenMVP.transform(point2);
		g2.drawLine((int) proj_p1.getX(), (int) proj_p1.getY(), (int) proj_p2.getX(), (int) proj_p2.getY());
		
		double dx = (0.10 * (point1.getX() - point2.getX()));
		double dy = (0.10 * (point1.getY() - point2.getY()));
		
		Point2D dot = new Point2D();
		dot.setX(dx);
		dot.setY(dy);
		dot.rotation(Math.PI / 8);
		dot.translation(point2.getX(), point2.getY());
		proj_p = screenMVP.transform(dot);
		g2.drawLine((int) proj_p2.getX(), (int) proj_p2.getY(), (int) proj_p.getX(), (int) proj_p.getY());
		
		dot.setX(dx);
		dot.setY(dy);
		dot.rotation(-Math.PI / 8);
		dot.translation(point2.getX(), point2.getY());
		proj_p = screenMVP.transform(dot);
		g2.drawLine((int) proj_p2.getX(), (int) proj_p2.getY(), (int) proj_p.getX(), (int) proj_p.getY());
	}
	
	public void drawArrow(Graphics g2, Point2D point, Vecteur2D vect) {
		Point2D p = new Point2D(point);
		p.translation(vect);
		drawArrow(g2, point, p);
	}
	
	public void drawBase(Graphics2D g2, Base2D base) {
		g2.setStroke(axisStroke);
		Point2D o = base.getOrigine();
		Vecteur2D ox = base.getOx();
		Vecteur2D oy = base.getOy();
		g2.setColor(Color.green);
		drawArrow(g2, o, ox);
		g2.setColor(Color.blue);
		drawArrow(g2, o, oy);
	}
	
	public void drawShape(Graphics2D g2, Shape2D shape) {
		int[] xpoints = new int[shape.getNPoint()];
		int[] ypoints = new int[shape.getNPoint()];
		for (int i = 0; i < shape.getNPoint(); i++) {
			Point2D proj_p = screenMVP.transform(shape.getPoint2D(i));
			xpoints[i] = (int) proj_p.getX();
			ypoints[i] = (int) proj_p.getY();
		}
		g2.setColor(shape.getColor());
		g2.fillPolygon(xpoints, ypoints, shape.getNPoint());
		if (shape.getStroke() != null) {
			g2.setColor(Color.black);
			g2.setStroke(shape.getStroke());
			g2.drawPolygon(xpoints, ypoints, shape.getNPoint());
		}
	}
	
	public void drawGrid(Graphics2D g2) {
		Point2D point1 = new Point2D();
		Point2D point2 = new Point2D();
		
		// Calcul des quatres points du rectangle (repere camera)
		Rectangle2D.Double rect = camera.getRectangle();
		Point2D bottomLeft = new Point2D(rect.getX(), rect.getY());
		Point2D bottomRight = new Point2D(rect.getX() + rect.getWidth(), rect.getY());
		Point2D topRight = new Point2D(rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight());
		Point2D topLeft = new Point2D(rect.getX(), rect.getY() + rect.getHeight());
		
		// Calcul des quatres points du rectangle (repere monde)
		Transformation2D inverseView = camera.viewMat().getInverseTransformation();
		bottomLeft = inverseView.transform(bottomLeft);
		bottomRight = inverseView.transform(bottomRight);
		topRight = inverseView.transform(topRight);
		topLeft = inverseView.transform(topLeft);
		
		// Unite de la grille
		double step = getUnity();
		
		// Calcul de la bounding box reguliere (repere monde)
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		//double step = 1;//(bottomRight.getX() - bottomLeft.getX()) / 10;
		for (Point2D point : new Point2D[] { bottomLeft, bottomRight, topRight, topLeft }) {
			if (point.getX() < minX) {
				minX = (int) (Math.floor(point.getX() / step) * step);
			}
			if (point.getX() > maxX) {
				maxX = (int) ((Math.floor(point.getX() / step) + 1) * step);
			}
			if (point.getY() < minY) {
				minY = (int) (Math.floor(point.getY() / step) * step);
			}
			if (point.getY() > maxY) {
				maxY = (int) ((Math.floor(point.getY() / step) + 1) * step);
			}
		}
		
		// Affichage de la grille reguliere
		point1.setY(minY);
		point2.setY(maxY);
		for (int x = minX; x <= maxX; x += step) {
			point1.setX(x);
			point2.setX(x);
			if (x == 0) {
				g2.setColor(Color.black);
				g2.setStroke(axisStroke);
			} else {
				g2.setColor(Color.gray);
				g2.setStroke(gridStroke);
			}
			drawLine(g2, point1, point2);
		}
		point1.setX(minX);
		point2.setX(maxX);
		for (int y = minY; y <= maxY; y += step) {
			point1.setY(y);
			point2.setY(y);
			if (y == 0) {
				g2.setColor(Color.black);
				g2.setStroke(axisStroke);
			} else {
				g2.setColor(Color.gray);
				g2.setStroke(gridStroke);
			}
			drawLine(g2, point1, point2);
		}
		
		// Affichage du repere canonique
		point1.setX(0);
		point1.setY(0);
		g2.setColor(Color.red);
		g2.setStroke(axisStroke);
		point2.setX(1);
		point2.setY(0);
		drawArrow(g2, point1, point2);
		point2.setX(0);
		point2.setY(1);
		drawArrow(g2, point1, point2);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.white);
		g2.fillRect(0, 0, getWidth(), getHeight());
		
		Transformation2D viewProj = Transformation2D.addTransformation(camera.projMat(), camera.viewMat());
		Transformation2D viewProjScreen = Transformation2D.addTransformation(viewport.screenMat(), viewProj);
		screenMVP = viewProjScreen;
		
		// Affichage de la grille
		drawGrid(g2);
		
		// Affichage des formes
		for (Shape2D shape : model.getListShape()) {
			drawShape(g2, shape);
			drawBase(g2, shape.getModel().toBase2D());
//			g2.setColor(Color.black);
//			drawPoint(g2, shape.getBarycenter());
		}
		
		if (eventButton == 2) {
			g2.setColor(Color.darkGray);
			g2.setStroke(gridStroke);
			g2.drawLine(getWidth() / 2, getHeight() / 2, columnClicked, lineClicked);
		}
	}
	
	
	/** Classe qui ecoute le modele et les click utilisateur */
	private class Handler extends MouseAdapter implements WorldModelListener, MouseMotionListener, MouseWheelListener, ComponentListener {

		///
		/// ComponentListener
		///
		@Override
		public void shapeAdded(Shape2D shape) { }

		@Override
		public void shapeRemoved(Shape2D shape) { }

		@Override
		public void needRefresh() {
			repaint();
		}
		
		///
		/// MouseListener
		///
		@Override
		public void mousePressed(MouseEvent ev) {
			columnClicked = ev.getX();
			lineClicked = ev.getY();
			eventButton = ev.getButton();
			
			if (eventButton == 1 && !getMoveable()) {
				columnClicked = 0;
				lineClicked = 0;
				eventButton = 0;
			}
			
			if (eventButton == 2 && !getSpinnable()) {
				columnClicked = 0;
				lineClicked = 0;
				eventButton = 0;
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent ev) {
			if (eventButton == 2) {
				repaint();
			}
			eventButton = 0;
		}
		
		@Override
		public void mouseDragged(MouseEvent ev) {
			Rectangle2D.Double rect = camera.getRectangle();
			double dx = ((double) (columnClicked - ev.getX()) / getWidth()) * rect.getWidth();
			double dy = ((double) (lineClicked - ev.getY()) / getHeight()) * rect.getHeight();
			
			// Left click
			if (eventButton == MouseEvent.BUTTON1) {
				camera.addTranslation(dx, -dy);
			}
			
			// Right click
			if (eventButton == MouseEvent.BUTTON2) {
				Vecteur2D or = new Vecteur2D(columnClicked - getWidth() / 2, lineClicked - getHeight() / 2);
				Vecteur2D op = new Vecteur2D(ev.getX() - getWidth() / 2, ev.getY() - getHeight() / 2);
				if (or.getNorme() > 0 && op.getNorme() > 0) {
					or.normalized();
					op.normalized();
					double radianOR = Math.atan2(or.getDy(), or.getDx());
					double radianOP = Math.atan2(op.getDy(), op.getDx());
					camera.addRotation(radianOP - radianOR);
				}
			}
			
			repaint();
			columnClicked = ev.getX();
			lineClicked = ev.getY();
		}
		
		///
		/// MouseWheelEvent
		///
		@Override
		public void mouseWheelMoved(MouseWheelEvent ev) {
			if (getZoomable()) {
				camera.addZoom(-ev.getWheelRotation() * (camera.getZ() / 10.0));
				repaint();
			}
		}
		
		///
		/// ComponentListener
		///
		@Override
		public void componentHidden(ComponentEvent ev) {}
		
		@Override
		public void componentMoved(ComponentEvent ev) {}
		
		@Override
		public void componentResized(ComponentEvent ev) {
			viewport.setWidth(getWidth());
			viewport.setHeight(getHeight());
			repaint();
		}
		
		@Override
		public void componentShown(ComponentEvent ev) {}		
	}
}