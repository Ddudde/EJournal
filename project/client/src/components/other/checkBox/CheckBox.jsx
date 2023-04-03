import React, {useEffect} from "react";
import {useDispatch, useSelector} from "react-redux";
import checkBoxCSS from './checkBox.module.css';
import {checkbox} from "../../../store/selector";
import {changeCB} from "../../../store/actions";


const CheckBox = (props) => {
    const checkBoxState = useSelector(checkbox);
    const dispatch = useDispatch();
    useEffect(() => {
        console.log("I was triggered during componentDidMount CheckBox.jsx");
        if(checkBoxState[props.checkbox_id] == undefined) {
            dispatch(changeCB(props.checkbox_id, props.state ? false : true));
        }
        return function() {
            console.log("I was triggered during componentWillUnmount CheckBox.jsx");
        }
    }, []);
    return (
        <div className={checkBoxCSS.block}>
            <input className={checkBoxCSS.inp}
                type="checkbox"
                {...props}
                checked={checkBoxState[props.checkbox_id] ? "checked" : ""}
                onChange={() => {dispatch(changeCB(props.checkbox_id, checkBoxState[props.checkbox_id]))}}
            />
            <div className={checkBoxCSS.tex}>
                {props.text}
            </div>
        </div>
    );
};
export default CheckBox;