import React from 'react';
import {render} from 'react-dom';

import 'bootstrap/dist/css/bootstrap.min.css';
import './oss.css'

import App from './App';

render(<React.StrictMode><App className={"test"}/></React.StrictMode>, document.getElementById('root'));
