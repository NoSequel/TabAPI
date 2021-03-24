# Tab API
TabAPI is an opensource tablist api I made for the spigot API. 

# Support
We currently support 2 NMS versions, ``v1_8_R3`` and ``v1_7_R4``, we're willing to support more versions in the future, and feel free to open a pull request if you are able to add a version.

# Known Issues
- There is some weird flickering on tab with the skulls, not the text, just the skulls.

# Usage
The usage is very easy - here's an example:

### Registering the handler itself:
Replace "v1_7_R4TabAdapter" with the version of NMS you're using (for example: v1_7_R4TabAdapter).
Replace "TabImpl" with the TabElementHandler you're using (this is where the tab api gets the elements from.)

```java
@Override
public void onEnable() {
    new TabHandler(new v1_7_R4TabAdapter(), new TabImpl(), this, 20L);
}
```

## Making a new TabElementHandler implementation
```java
public class TabImpl implements TabElementHandler {

    /**
     * Get the tab element of a player
     *
     * @param player the player
     * @return the element
     */
    @Override
    public TabElement getElement(Player player) {
        final TabElement element = new TabElement();

        for (int i = 0; i < 80; i++) {
            final int x = i % 4;
            final int y = i / 4;

            element.add(x, y, ChatColor.GREEN + "Slot: " + ChatColor.GRAY + x + ", " + y);
        }

        return element;
    }
}
```

# Selling & Using
You're free to use this product for anything, including selling and running on your own server. However, if you're going to sell a plugin using this, please leave credits and/or a link to the repository.