
An incubating 2D scroll helper.

- Measures the size and position of UI layouts on the screen
- Asynchronous process continuously fetches views closest to scroll position
- Adjusts X/Y of view containers
- Binds data to the containers

Example data mapping given scroll position of x: 4, y: 4 and containers A, B, C, and D:


         DATA        SCREEN   CONTAINERS  
    00 01 02 03 04  . . . . .  44 -> A  
    10 11 12 13 14  . . . . .  43 -> B  
    20 21 22 23 24  . . . _ .  33 -> C  
    30 31 32 33 34  . . |   |  34 -> D  
    40 41 42 43 44  . . | _ |  

Filter and sort of list is attained by updating indexes of visible views, and does not need data reload:

                   INDEX STRUCTURE
                   
    DATA
    index | name | ... | #
        0                # Array of
        .                # table data
        N                #
        
    INDEX
    index | DATA         # Array of sorted
        0      0         # and filtered
        .      .         # data indexes
        M      N         # M <= N
         
    DISPLAY
    index | XYWH | DATA  #
        0             0  # Index of available
        .             .  # view containers
        K             N  # K <= M


"Experiemental" mode directs scrolling entirely to the utility's internal rendering engine. At that point all view containers and their positioning is handled internally without relying on high-level system UI libraries or context.

A UI element is rendered from JSON representation of a regular UI element:

    {
      "element": "ROW",
      "background": "#000022",
      "paddingStart": 8,
      "paddingEnd": 8,
      "children": [
        {
          "element": "IMAGE",
          "w": 194,
          "h": 194,
          "image": "%ICON%"
        },
        {
          "element": "COLUMN",
          "paddingStart": 8,
          "paddingEnd": 8,
          "children": [
            {
              "element": "TEXT",
              "text": "%TITLE%",
              "size": 48
            },
            {
              "element": "TEXT",
              "text": "%ORIGIN%",
              "size": 44
            },
            {
              "element": "TEXT",
              "text": "%DESCRIPTION%",
              "size": 36
            }
          ]
        }
      ]
    }

mode enables scrolling without relying on the native Android UI library. Instead it is performed by monitoring swipe gestures  
and re-computing view positions on each pass. It is under development with the eventual aim of transpiling the code onto a device without a traditional OS -  
for example, a low-cost, secure device that presents very large scroll view of expandable layouts.

