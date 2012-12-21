package plugins.adufour.multitouch;

import icy.canvas.Canvas2D;
import icy.canvas.Canvas3D;
import icy.canvas.IcyCanvas;
import icy.gui.frame.progress.AnnounceFrame;
import icy.gui.main.MainInterface;
import icy.gui.viewer.Viewer;
import icy.main.Icy;
import icy.plugin.abstract_.PluginActionable;
import icy.system.IcyHandledException;

import javax.vecmath.Vector2f;

import vtk.vtkCamera;
import vtk.vtkRenderer;

public class MultiTouchCanvas extends PluginActionable
{
    private MultiTouchProvider provider;
    
    @Override
    public void run()
    {
        try
        {
            provider = new MultiTouchProvider();
            final TwoFingersListener twoFingersListener = new MultiTouchActor();
            
            provider.addTwoFingersListener(twoFingersListener);
            
            new AnnounceFrame("Multi-touch gestures activated...", "De-activate", new Runnable()
            {
                @Override
                public void run()
                {
                    if (twoFingersListener != null) provider.removeTwoFingersListener(twoFingersListener);
                }
            }, 0);
        }
        catch (ExceptionInInitializerError initError)
        {
            throw new RuntimeException("Error initializing multi-touch provider" + initError.getMessage());
        }
        
    }
    
    private IcyCanvas getCanvas()
    {
        MainInterface main = Icy.getMainInterface();
        
        if (main == null) throw new IcyHandledException("No main interface (is Icy running headless?)");
        
        Viewer viewer = main.getFocusedViewer();
        
        return viewer == null ? null : viewer.getCanvas();
    }
    
    private class MultiTouchActor implements TwoFingersListener
    {
        @Override
        public void rotate(MultiTouchProvider source, float angle)
        {
            IcyCanvas canvas = getCanvas();
            
            if (canvas instanceof Canvas2D)
            {
                Canvas2D c2D = (Canvas2D) canvas;
                c2D.setRotation(c2D.getRotationZ() - angle * 10, true);
            }
            else if (canvas instanceof Canvas3D)
            {
                Canvas3D c3d = (Canvas3D) canvas;
                vtkRenderer ren = c3d.getRenderer();
                vtkCamera cam = ren.GetActiveCamera();
                cam.Roll(angle * 360);
                c3d.getPanel3D().repaint();
            }
        }
        
        @Override
        public void pinch(MultiTouchProvider source, float delta)
        {
            IcyCanvas canvas = getCanvas();
            
            if (canvas instanceof Canvas2D)
            {
                Canvas2D c2D = (Canvas2D) canvas;
                double newScale = c2D.getScaleX() - delta * 10;
                c2D.setScale(newScale, newScale, false, true);
            }
            else if (canvas instanceof Canvas3D)
            {
                Canvas3D c3D = (Canvas3D) canvas;
                
                vtkCamera cam = c3D.getRenderer().GetActiveCamera();
                cam.Zoom(1.0 - delta * 4);
                c3D.getPanel3D().repaint();
            }
        }
        
        @Override
        public void drag(MultiTouchProvider source, Vector2f direction, float delta)
        {
            IcyCanvas canvas = getCanvas();
            
            if (canvas instanceof Canvas2D)
            {
                Canvas2D c2D = (Canvas2D) canvas;
                direction.scale(10000 * delta);
                c2D.setOffset(c2D.getOffsetX() + Math.round(direction.x), c2D.getOffsetY() - Math.round(direction.y), true);
            }
            else if (canvas instanceof Canvas3D)
            {
                direction.scale(1000 * delta);
                
                Canvas3D c3D = (Canvas3D) canvas;
                
                // vtkRenderWindowInteractor interactor = ren.GetRenderWindow().GetInteractor();
                // if (interactor == null) // FIXME this is always the case...
                // {
                // interactor = ren.GetRenderWindow().MakeRenderWindowInteractor();
                // ren.GetRenderWindow().SetInteractor(interactor);
                // }
                //
                // int x = 0, y = 0;
                // interactor.SetEventInformation(x, y, 0, 0, ' ', 0, "");
                // interactor.MiddleButtonPressEvent();
                // x += direction.x;
                // y += direction.y;
                // interactor.SetEventInformation(x, y, 0, 0, ' ', 0, "");
                // interactor.MiddleButtonReleaseEvent();
                
                vtkCamera cam = c3D.getRenderer().GetActiveCamera();
                
                double[] pos = cam.GetPosition();
                pos[0] -= direction.x;
                pos[1] += direction.y;
                cam.SetPosition(pos);
                pos = cam.GetFocalPoint();
                pos[0] -= direction.x;
                pos[1] += direction.y;
                cam.SetFocalPoint(pos);
                c3D.getPanel3D().repaint();
                
            }
        }
    }
}
