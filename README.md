An incubating 2D scroll helper.

- Measures the size and position of UI layouts on the screen
- Continuously fetches views closest to scroll position
- Applies offset X/Y to view containers
- Binds data to the containers

Example data mapping with containers A, B, C, D given scroll position of bottom right x: 4, y: 4:

         DATA        SCREEN   CONTAINERS  
    00 01 02 03 04  . . . . .  44 -> A  
    10 11 12 13 14  . . . . .  43 -> B  
    20 21 22 23 24  . . . _ .  33 -> C  
    30 31 32 33 34  . . |   |  34 -> D  
    40 41 42 43 44  . . | _ |  

Filter and sort of list is attained by updating indexes of visible views, and does not need data reload:

    STRUCTURE:

    DATA
    # | col1 | col2 |    #
    0                    #
    .                    #
    N                    # Table data
        
    FILTER
    # | DATA.#           # 
    0        0           # Sorted, filtered
    .        .           # data indexes
    M        N           # M <= N
         
    DISPLAY
    # | FILTER.# | XYWH  #
    0          0         # View container
    .          .         # mapping
    L          N         # L <= M

### 2D Scrolling
<img src="https://raw.githubusercontent.com/sheinin/myguide/main/docs/2d.png" height=200>

### Text Size Scaling and Zoom
<img src="https://raw.githubusercontent.com/sheinin/myguide/main/docs/zoom.png" width=200>

### Collapsible Tree View
<img src="https://raw.githubusercontent.com/sheinin/myguide/main/docs/tree.png" width=200>


## Experimental Mode

Rendering and positioning of layouts is handled internally, without relying on UI libraries or context.

Layout templates are sourced from [serialized strings](https://raw.githubusercontent.com/sheinin/myguide/main/docs/view.json)

In Experimental Mode, UI rendering relies on algebraic calculations with no need for external dependencies. This approach promises new Browsing capabilities to low power devices.


[Browser prototype](https://sheinin.github.io/sheinin/) completed in 2020.