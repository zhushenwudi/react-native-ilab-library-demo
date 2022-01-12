import * as React from 'react';

import {Button, StyleSheet, Text, View} from 'react-native';
import {addReadCardCallback, readCard, releaseReadCard} from 'react-native-ilab-library-demo';

export default function App() {
    const [result, setResult] = React.useState<string>();

    React.useEffect(() => {
        addReadCardCallback((data: string) => {
            setResult(data)
        })
        return () => releaseReadCard()
    }, []);

    return (
        <View style={styles.container}>
            <Text>Result: {result}</Text>
            <Button title={"点击读取"} onPress={readCard}/>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center',
    },
    box: {
        width: 60,
        height: 60,
        marginVertical: 20,
    },
});
