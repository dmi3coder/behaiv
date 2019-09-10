# BehAIv
#### Behaiv is a tool that helps users to navigate faster through the app. Largely inspired by app suggestion in IOS.
Predict what users want to open and take appropriate actions
### How to use in Android?

1. Add dependency
1. Instantiate `BehAIv` object in *ApplicationContext*
1. Select one of a Kernel types
    1. `DummyKernel` doesn't do any computations, only suggest most similar result by comparing count of steps
    1. `RNNKernel` uses Recurrent Neural Network to specify actions.
    1. `RemoteKernel`, sends data to a API, depending on type of an API receives suggested action or receives model
1. Set threshold after which Behaiv can start suggesting
1. Register each view that can be tracked and opened by implementing interfaces
    1. `InitiableNode` is a type of view that can initiate capturing of features
    1. `ActionableNode` is a type of view that can be opened by *Behaiv*, as well as capture labels
    1. `RoutableNode` is a type of View that only cannot be opened but only route into next Views
    1. `ConditionableNode` is a type of View which will perform additional actions before proceeding
1. Provide external factors sich as GPS, Wifi/Bluetooth and headphons info.
    1. Use `GeoProvider` for adding GPS feature into prediction
    1. Use `NetworkProvider` for adding Wifi, Bluetooth and Network features into prediction
    1. Use `HeadsetProvider` to include headphones feature into predcition
    1. There's more options like Weather and Firebase provides, see more at (TODO)
    1. Use `SyntheticProvide` to include custom self-made features into prediction (e.g )
1. Model uses "Continuous learning" so it will be trained as long as more features will arrive. To see model in action you need to wait amount of examples reach threshold.

   
