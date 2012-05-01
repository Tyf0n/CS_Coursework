import java.util.*;
import java.security.*;

/**
   A SecurityManager that prohibits all contexts except for 
   the current one from using any runtime activities except
   those specifically allowed.  By default, all other contexts
   will execute as if they were Applets (i.e., unable
   to touch the file system, reflection, etc.
*/
public class MaximumSecurityManager extends SecurityManager {
        
    /** Set of all permitted runtime actions (by default, all others are blocked.)
        See http://java.sun.com/j2se/1.4.2/docs/api/java/lang/RuntimePermission.html for the 
        list of runtime permissions. */
    final private HashSet<String> runtimePermissions = new HashSet<String>();

    /** The context that created this security manager,
        which will remain unrestricted.
     */
    private AccessControlContext whitelistContext;

    public MaximumSecurityManager() {
        whitelistContext = AccessController.getContext();
    }
    
    public MaximumSecurityManager(String[] permissions) {
        this();
        runtimePermissions.addAll(Arrays.asList(permissions));
    }

    public void checkPermission(Permission perm) {
        if (! getSecurityContext().equals(whitelistContext) &&
            (perm instanceof RuntimePermission) && 
            ! runtimePermissions.contains(perm.getName())) {

            throw new SecurityException("Not allowed to " + perm.getName());
        }
    }
}
