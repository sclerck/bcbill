import React from 'react';
import { connect } from 'react-redux';
import { bindActionCreators } from 'redux';
import * as Actions from './../actions/ActionsCreators.js';

function mapStateToProps(state) {
  return {
    isConnected: state.connectionStatus.status
  };
}

function mapDispatchToProps(dispatch) {
  return {
    actions: bindActionCreators(Actions, dispatch)
  };
}

class Connect extends React.Component {

  handleClick() {
    this.props.isConnected ? this.props.actions.disconnect() : this.props.actions.connect()
  }

  renderBtn() {
    if (this.props.isConnected) {
      return <button style={styles.connectBtn} type="button" onClick={() => this.handleClick()}>Disconnect</button>
    } else {
      return <button style={styles.connectBtn} type="button" onClick={() => this.handleClick()}>Connect</button>
    }
  }

  render(){
    return (
      <div style={styles.container}>
        {
          this.renderBtn()
        }
      </div>
    );
  }

}

const styles = {
  hidden: {
    display: 'none'
  },
  input: {
    border: '1px solid black',
    borderRadius: '2px',
    padding: '5px 6px',
    display: 'block',
    marginBottom: '10px',
    width: '85%'
  },
  container: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    flexDirection: 'column'
  },
  connectBtn: {
    padding: '9px 6px',
    width: '89%',
    fontSize: '15px'
  }

}


export default connect(mapStateToProps, mapDispatchToProps)(Connect);
